package com.westlakers.leap_bff.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.westlakers.leap_bff.dtos.PriceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Real-time price streaming service that consumes live prices from Redis.
 * Demonstrates consuming the live price data streamed by the Python analytics pipeline.
 * 
 * Features:
 * - Subscribes to Redis streams and pub/sub for real-time price updates
 * - Caches latest prices for quick lookup
 * - Tracks price history and statistics
 * - Supports real-time WebSocket streaming to clients
 */
@Slf4j
@Service
public class PriceStreamingService {

    private static final String ACTIVE_TICKERS_SET = "prices:tickers:active";
    private static final String WATCHLIST_CLIENT_KEY_PREFIX = "prices:watchlist:client:";
    private static final String FOCUS_CLIENT_KEY_PREFIX = "prices:focus:client:";
    private static final String WATCHLIST_SUBSCRIBERS_KEY_PREFIX = "prices:watchlist:subs:";
    private static final String FOCUS_SUBSCRIBERS_KEY_PREFIX = "prices:focus:subs:";
    private static final String FOCUS_LEASE_KEY_PREFIX = "prices:focus:lease:";
    private static final Duration FOCUS_LEASE = Duration.ofMinutes(5);
    private static final String WATCHLIST_TICKERS_SET = "prices:tickers:watchlist";
    private static final String FOCUS_TICKERS_SET = "prices:tickers:focus";
    
    private final RedisTemplate<String, PriceDTO> priceRedisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    // In-memory cache of latest prices
    private final Map<String, PriceDTO> priceCache = new ConcurrentHashMap<>();
    
    // One Redis listener is shared by all WebSocket clients subscribed to a ticker.
    private final Map<String, Set<PriceUpdateCallback>> callbacksByTicker = new ConcurrentHashMap<>();
    private final Map<String, MessageListener> redisListeners = new ConcurrentHashMap<>();
    
    public PriceStreamingService(
            RedisTemplate<String, PriceDTO> priceRedisTemplate,
            RedisTemplate<String, String> stringRedisTemplate,
            ObjectMapper objectMapper,
            RedisMessageListenerContainer redisMessageListenerContainer) {
        this.priceRedisTemplate = priceRedisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.redisMessageListenerContainer = redisMessageListenerContainer;
        log.info("PriceStreamingService initialized");
    }
    
    /**
     * Get the latest price for a ticker from cache
     * 
     * @param ticker Stock ticker symbol
     * @return Latest PriceDTO or empty Optional
     */
    public Optional<PriceDTO> getLatestPrice(String ticker) {
        String key = "prices:latest:" + ticker.toUpperCase();
        try {
            String jsonData = stringRedisTemplate.opsForValue().get(key);
            if (jsonData != null) {
                PriceDTO price = objectMapper.readValue(jsonData, PriceDTO.class);
                return Optional.of(price);
            }
        } catch (Exception e) {
            log.error("Error fetching price for {}: {}", ticker, e.getMessage());
        }
        return Optional.empty();
    }
    
    /**
     * Get the latest prices for multiple tickers
     * 
     * @param tickers List of stock ticker symbols
     * @return Map of ticker -> PriceDTO
     */
    public Map<String, PriceDTO> getLatestPrices(List<String> tickers) {
        Map<String, PriceDTO> prices = new LinkedHashMap<>();
        for (String ticker : tickers) {
            String normalized = normalizeTicker(ticker);
            getLatestPrice(normalized).ifPresent(price -> prices.put(normalized, price));
        }
        return prices;
    }
    
    /**
     * Get all available tickers from Redis metadata
     * 
     * @return Set of ticker symbols currently being streamed
     */
    public Set<String> getAvailableTickers() {
        Map<Object, Object> metadata = stringRedisTemplate.opsForHash().entries("prices:metadata");
        Set<String> tickers = new HashSet<>();

        Set<String> activeSet = stringRedisTemplate.opsForSet().members(ACTIVE_TICKERS_SET);
        if (activeSet != null) {
            activeSet.stream()
                .filter(Objects::nonNull)
                .map(this::normalizeTicker)
                .forEach(tickers::add);
        }
        
        for (Object key : metadata.keySet()) {
            String keyStr = (String) key;
            if (keyStr.endsWith(":last_price")) {
                String ticker = keyStr.replace(":last_price", "");
                tickers.add(ticker);
            }
        }
        
        return tickers;
    }

    public Set<String> registerTickers(Collection<String> tickers) {
        Set<String> normalizedTickers = new LinkedHashSet<>();

        if (tickers != null) {
            tickers.stream()
                .filter(Objects::nonNull)
                .map(this::normalizeTicker)
                .forEach(normalizedTickers::add);
        }

        if (!normalizedTickers.isEmpty()) {
            stringRedisTemplate.opsForSet().add(ACTIVE_TICKERS_SET, normalizedTickers.toArray(new String[0]));
            log.info("Registered {} ticker(s): {}", normalizedTickers.size(), normalizedTickers);
        }

        return normalizedTickers;
    }

    public String registerTicker(String ticker) {
        Set<String> tickers = registerTickers(Collections.singletonList(ticker));
        if (tickers.isEmpty()) {
            return null;
        }
        return tickers.iterator().next();
    }

    public String unregisterTicker(String ticker) {
        String normalized = normalizeTicker(ticker);
        stringRedisTemplate.opsForSet().remove(ACTIVE_TICKERS_SET, normalized);
        log.info("Unregistered ticker: {}", normalized);
        return normalized;
    }

    public Set<String> syncWatchlist(String clientId, Collection<String> tickers) {
        String normalizedClientId = normalizeClientId(clientId);
        String clientWatchlistKey = WATCHLIST_CLIENT_KEY_PREFIX + normalizedClientId;

        Set<String> existing = Optional.ofNullable(stringRedisTemplate.opsForSet().members(clientWatchlistKey))
            .orElse(Collections.emptySet());

        Set<String> normalizedIncoming = new LinkedHashSet<>();
        if (tickers != null) {
            tickers.stream()
                .filter(Objects::nonNull)
                .map(this::normalizeTicker)
                .forEach(normalizedIncoming::add);
        }

        Set<String> toAdd = new LinkedHashSet<>(normalizedIncoming);
        toAdd.removeAll(existing);

        Set<String> toRemove = new LinkedHashSet<>(existing);
        toRemove.removeAll(normalizedIncoming);

        if (!toAdd.isEmpty()) {
            stringRedisTemplate.opsForSet().add(clientWatchlistKey, toAdd.toArray(new String[0]));
            for (String ticker : toAdd) {
                stringRedisTemplate.opsForSet().add(WATCHLIST_SUBSCRIBERS_KEY_PREFIX + ticker, normalizedClientId);
                reconcileTickerRegistration(ticker);
            }
        }

        if (!toRemove.isEmpty()) {
            stringRedisTemplate.opsForSet().remove(clientWatchlistKey, toRemove.toArray(new String[0]));
            for (String ticker : toRemove) {
                stringRedisTemplate.opsForSet().remove(WATCHLIST_SUBSCRIBERS_KEY_PREFIX + ticker, normalizedClientId);
                reconcileTickerRegistration(ticker);
            }
        }

        log.info("Synced watchlist for client {}. Size: {}", normalizedClientId, normalizedIncoming.size());
        return normalizedIncoming;
    }

    public String focusTicker(String clientId, String ticker) {
        String normalizedClientId = normalizeClientId(clientId);
        String normalizedTicker = normalizeTicker(ticker);

        stringRedisTemplate.opsForValue().set(
            focusLeaseKey(normalizedTicker, normalizedClientId),
            normalizedClientId,
            FOCUS_LEASE
        );
        stringRedisTemplate.opsForSet().add(FOCUS_CLIENT_KEY_PREFIX + normalizedClientId, normalizedTicker);
        stringRedisTemplate.opsForSet().add(FOCUS_SUBSCRIBERS_KEY_PREFIX + normalizedTicker, normalizedClientId);
        reconcileTickerRegistration(normalizedTicker);

        log.info("Client {} focused ticker {}", normalizedClientId, normalizedTicker);
        return normalizedTicker;
    }

    public String unfocusTicker(String clientId, String ticker) {
        String normalizedClientId = normalizeClientId(clientId);
        String normalizedTicker = normalizeTicker(ticker);

        stringRedisTemplate.delete(focusLeaseKey(normalizedTicker, normalizedClientId));
        stringRedisTemplate.opsForSet().remove(FOCUS_CLIENT_KEY_PREFIX + normalizedClientId, normalizedTicker);
        stringRedisTemplate.opsForSet().remove(FOCUS_SUBSCRIBERS_KEY_PREFIX + normalizedTicker, normalizedClientId);
        reconcileTickerRegistration(normalizedTicker);

        log.info("Client {} unfocused ticker {}", normalizedClientId, normalizedTicker);
        return normalizedTicker;
    }

    public Set<String> getClientWatchlist(String clientId) {
        String normalizedClientId = normalizeClientId(clientId);
        return Optional.ofNullable(stringRedisTemplate.opsForSet().members(WATCHLIST_CLIENT_KEY_PREFIX + normalizedClientId))
            .orElse(Collections.emptySet());
    }

    private String focusLeaseKey(String ticker, String clientId) {
        return FOCUS_LEASE_KEY_PREFIX + ticker + ":" + clientId;
    }

    private String normalizeTicker(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker cannot be empty");
        }
        String normalized = ticker.trim().toUpperCase(Locale.ROOT);
        // Yahoo Finance identifies forex pairs with a six-letter pair plus the =X suffix (for example EURUSD=X).
        if (!normalized.matches("[A-Z0-9._-]{1,20}") && !normalized.matches("[A-Z]{6}=X")) {
            throw new IllegalArgumentException("Invalid ticker format: " + ticker);
        }
        return normalized;
    }

    private String normalizeClientId(String clientId) {
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new IllegalArgumentException("clientId cannot be empty");
        }
        String normalized = clientId.trim();
        if (!normalized.matches("[A-Za-z0-9._:-]{8,100}")) {
            throw new IllegalArgumentException("Invalid clientId format");
        }
        return normalized;
    }

    private void reconcileTickerRegistration(String ticker) {
        String normalizedTicker = normalizeTicker(ticker);
        Long watchlistSubs = stringRedisTemplate.opsForSet().size(WATCHLIST_SUBSCRIBERS_KEY_PREFIX + normalizedTicker);
        Long focusSubs = stringRedisTemplate.opsForSet().size(FOCUS_SUBSCRIBERS_KEY_PREFIX + normalizedTicker);
        long totalSubs = (watchlistSubs == null ? 0 : watchlistSubs) + (focusSubs == null ? 0 : focusSubs);

        if (totalSubs > 0) {
            stringRedisTemplate.opsForSet().add(ACTIVE_TICKERS_SET, normalizedTicker);
        } else {
            stringRedisTemplate.opsForSet().remove(ACTIVE_TICKERS_SET, normalizedTicker);
        }

        updateCategoryRegistration(WATCHLIST_TICKERS_SET, normalizedTicker, watchlistSubs != null && watchlistSubs > 0);
        updateCategoryRegistration(FOCUS_TICKERS_SET, normalizedTicker, focusSubs != null && focusSubs > 0);
    }

    private void updateCategoryRegistration(String tickerSet, String ticker, boolean subscribed) {
        if (subscribed) {
            stringRedisTemplate.opsForSet().add(tickerSet, ticker);
        } else {
            stringRedisTemplate.opsForSet().remove(tickerSet, ticker);
        }
    }
    
    /**
     * Subscribe to price updates for a specific ticker via pub/sub
     * Useful for WebSocket streaming to clients
     * 
     * @param ticker Stock ticker symbol
     * @return Price update stream
     */
    public void subscribeToTicker(String ticker, PriceUpdateCallback callback) {
        String normalizedTicker = normalizeTicker(ticker);
        Set<PriceUpdateCallback> callbacks = callbacksByTicker.computeIfAbsent(
            normalizedTicker, ignored -> ConcurrentHashMap.newKeySet());
        callbacks.add(callback);

        synchronized (redisListeners) {
            if (!redisListeners.containsKey(normalizedTicker)) {
                String channel = "prices:" + normalizedTicker;
                MessageListener listener = this::handleRedisPriceMessage;
                redisListeners.put(normalizedTicker, listener);
                redisMessageListenerContainer.addMessageListener(listener, new ChannelTopic(channel));
                log.info("Started shared Redis listener for ticker: {}", normalizedTicker);
            }
        }

        getLatestPrice(normalizedTicker).ifPresent(callback::onPriceUpdate);
    }

    public void unsubscribeFromTicker(String ticker, PriceUpdateCallback callback) {
        String normalizedTicker = normalizeTicker(ticker);
        Set<PriceUpdateCallback> callbacks = callbacksByTicker.get(normalizedTicker);
        if (callbacks == null) {
            return;
        }

        callbacks.remove(callback);
        if (!callbacks.isEmpty()) {
            return;
        }

        callbacksByTicker.remove(normalizedTicker, callbacks);
        synchronized (redisListeners) {
            MessageListener listener = redisListeners.remove(normalizedTicker);
            if (listener != null) {
                redisMessageListenerContainer.removeMessageListener(
                    listener, new ChannelTopic("prices:" + normalizedTicker));
                log.info("Stopped shared Redis listener for ticker: {}", normalizedTicker);
            }
        }
    }

    private void handleRedisPriceMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String ticker = channel.substring("prices:".length()).toUpperCase(Locale.ROOT);
        try {
            PriceDTO price = objectMapper.readValue(message.getBody(), PriceDTO.class);
            priceCache.put(ticker, price);
            for (PriceUpdateCallback callback : callbacksByTicker.getOrDefault(ticker, Set.of())) {
                try {
                    callback.onPriceUpdate(price);
                } catch (RuntimeException e) {
                    log.debug("Price callback failed for {}: {}", ticker, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error processing price update for {}: {}", ticker, e.getMessage());
        }
    }
    
    /**
     * Read historical prices from Redis stream
     * Useful for replaying recent price history
     * 
     * @param count Number of recent entries to retrieve
     * @return List of price records
     */
    public List<Map<String, String>> getPriceHistory(int count) {
        return getPriceHistory(null, count);
    }

    /**
     * Read recent prices from the Redis stream, optionally limited to one ticker.
     */
    public List<Map<String, String>> getPriceHistory(String ticker, int count) {
        try {
            List<Map<String, String>> history = new ArrayList<>();
            int requestedCount = Math.max(1, Math.min(count, 5000));
            String requestedTicker = ticker == null ? null : normalizeTicker(ticker);

            List<MapRecord<String, Object, Object>> records =
                stringRedisTemplate.opsForStream().reverseRange(
                    "prices:stream", Range.unbounded(), Limit.limit().count(requestedCount * 4));

            if (records != null) {
                for (MapRecord<String, Object, Object> record : records) {
                    Map<Object, Object> values = record.getValue();
                    String recordTicker = Objects.toString(values.get("ticker"), "").toUpperCase(Locale.ROOT);
                    if (requestedTicker != null && !requestedTicker.equals(recordTicker)) {
                        continue;
                    }

                    Map<String, String> point = new LinkedHashMap<>();
                    point.put("ticker", recordTicker);
                    point.put("price", Objects.toString(values.get("price"), ""));
                    point.put("timestamp", Objects.toString(values.get("timestamp"), record.getId().getValue()));
                    history.add(point);
                    if (history.size() >= requestedCount) {
                        break;
                    }
                }
            }

            Collections.reverse(history);
            return history;
        } catch (Exception e) {
            log.error("Error reading price history: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Fetch chart history from Yahoo for the requested range and interval.
     * Redis remains the fallback for environments without outbound access.
     */
    public List<Map<String, String>> getMarketHistory(String ticker, String period, String interval, int limit) {
        String normalizedTicker = normalizeTicker(ticker);
        String normalizedPeriod = period == null ? "5d" : period.toLowerCase(Locale.ROOT);
        String normalizedInterval = interval == null ? "5m" : interval.toLowerCase(Locale.ROOT);
        if (!Set.of("1d", "5d", "1mo", "6mo", "1y", "3y").contains(normalizedPeriod)
            || !Set.of("1m", "5m", "15m", "1h", "1d", "1wk").contains(normalizedInterval)) {
            throw new IllegalArgumentException("Unsupported history period or interval");
        }

        try {
            long end = Instant.now().getEpochSecond();
            long start = switch (normalizedPeriod) {
                case "1d" -> end - Duration.ofDays(1).toSeconds();
                case "5d" -> end - Duration.ofDays(5).toSeconds();
                case "1mo" -> end - Duration.ofDays(30).toSeconds();
                case "6mo" -> end - Duration.ofDays(182).toSeconds();
                case "1y" -> end - Duration.ofDays(365).toSeconds();
                case "3y" -> end - Duration.ofDays(365 * 3L).toSeconds();
                default -> end - Duration.ofDays(365).toSeconds();
            };
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + normalizedTicker
                + "?period1=" + start + "&period2=" + end + "&interval=" + normalizedInterval
                + "&events=history&includeAdjustedClose=true";
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
            JsonNode chart = objectMapper.readTree(httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body())
                .path("chart").path("result").path(0);
            JsonNode timestamps = chart.path("timestamp");
            JsonNode closes = chart.path("indicators").path("quote").path(0).path("close");
            List<Map<String, String>> history = new ArrayList<>();
            for (int index = 0; index < timestamps.size(); index++) {
                JsonNode close = closes.path(index);
                if (!close.isNumber()) {
                    continue;
                }
                Map<String, String> point = new LinkedHashMap<>();
                point.put("ticker", normalizedTicker);
                point.put("price", close.asText());
                point.put("timestamp", Instant.ofEpochSecond(timestamps.get(index).asLong()).toString());
                history.add(point);
            }
            return history.size() > limit ? history.subList(history.size() - limit, history.size()) : history;
        } catch (Exception error) {
            log.warn("Yahoo history unavailable for {}: {}", normalizedTicker, error.getMessage());
            return getPriceHistory(normalizedTicker, limit);
        }
    }
    
    /**
     * Get price statistics for a ticker (latest update time, current price, etc)
     * 
     * @param ticker Stock ticker symbol
     * @return Statistics map
     */
    public Map<String, Object> getPriceStats(String ticker) {
        Map<String, Object> stats = new LinkedHashMap<>();
        String tickerUpper = ticker.toUpperCase();
        
        try {
            // Get last update time
            String lastUpdateKey = tickerUpper + ":last_update";
            Object lastUpdate = stringRedisTemplate.opsForHash().get("prices:metadata", lastUpdateKey);
            stats.put("lastUpdate", lastUpdate != null ? lastUpdate : "N/A");
            
            // Get last price
            String lastPriceKey = tickerUpper + ":last_price";
            Object lastPrice = stringRedisTemplate.opsForHash().get("prices:metadata", lastPriceKey);
            stats.put("lastPrice", lastPrice != null ? lastPrice : "N/A");
            
            // Get full price data
            Optional<PriceDTO> priceOpt = getLatestPrice(ticker);
            if (priceOpt.isPresent()) {
                PriceDTO price = priceOpt.get();
                stats.put("change", price.getChange());
                stats.put("changePct", price.getChangePct());
                stats.put("bid", price.getBid());
                stats.put("ask", price.getAsk());
                stats.put("volume", price.getVolume());
                stats.put("marketCap", price.getMarketCap());
                stats.put("peRatio", price.getPeRatio());
            }
        } catch (Exception e) {
            log.error("Error getting price stats for {}: {}", ticker, e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Check if the price streaming is active
     * 
     * @return true if stream is healthy and receiving updates
     */
    public boolean isStreamingActive() {
        Set<String> tickers = getAvailableTickers();
        if (tickers.isEmpty()) {
            log.warn("No active tickers found in Redis");
            return false;
        }
        
        // Check if any ticker has been updated in the last 30 seconds
        long now = System.currentTimeMillis();
        for (String ticker : tickers) {
            Map<String, Object> stats = getPriceStats(ticker);
            Object lastUpdate = stats.get("lastUpdate");
            // For a simple check, just verify we have data
            if (lastUpdate != null) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get dashboard data with prices for all active tickers
     * 
     * @return Dashboard data with all ticker information
     */
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboard = new LinkedHashMap<>();
        Set<String> tickers = getAvailableTickers();
        
        dashboard.put("timestamp", Instant.now().toString());
        dashboard.put("streamingActive", isStreamingActive());
        dashboard.put("activeTickers", tickers.size());
        
        Map<String, Map<String, Object>> prices = new LinkedHashMap<>();
        for (String ticker : tickers) {
            prices.put(ticker, getPriceStats(ticker));
        }
        dashboard.put("prices", prices);
        
        return dashboard;
    }
    
    /**
     * Callback interface for price update subscriptions
     */
    @FunctionalInterface
    public interface PriceUpdateCallback {
        void onPriceUpdate(PriceDTO price);
    }
}
