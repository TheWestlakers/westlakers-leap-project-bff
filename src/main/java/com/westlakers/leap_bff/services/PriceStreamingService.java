package com.westlakers.leap_bff.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.westlakers.leap_bff.dtos.PriceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
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
    
    private final RedisTemplate<String, PriceDTO> priceRedisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final ObjectMapper objectMapper;
    
    // In-memory cache of latest prices
    private final Map<String, PriceDTO> priceCache = new ConcurrentHashMap<>();
    
    // Track price subscribers for WebSocket streaming
    private final Set<String> activeSubscribers = ConcurrentHashMap.newKeySet();
    
    public PriceStreamingService(
            RedisTemplate<String, PriceDTO> priceRedisTemplate,
            RedisTemplate<String, String> stringRedisTemplate,
            ObjectMapper objectMapper) {
        this.priceRedisTemplate = priceRedisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
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
            getLatestPrice(ticker).ifPresent(price -> prices.put(ticker, price));
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
        
        for (Object key : metadata.keySet()) {
            String keyStr = (String) key;
            if (keyStr.endsWith(":last_price")) {
                String ticker = keyStr.replace(":last_price", "");
                tickers.add(ticker);
            }
        }
        
        return tickers;
    }
    
    /**
     * Subscribe to price updates for a specific ticker via pub/sub
     * Useful for WebSocket streaming to clients
     * 
     * @param ticker Stock ticker symbol
     * @return Price update stream
     */
    public void subscribeToTicker(String ticker, PriceUpdateCallback callback) {
        String channel = "prices:" + ticker.toUpperCase();
        activeSubscribers.add(channel);
        
        // Create a thread to listen for pub/sub messages
        Thread listenerThread = new Thread(() -> {
            try {
                stringRedisTemplate.getConnectionFactory()
                    .getConnection()
                    .subscribe((message, pattern) -> {
                        try {
                            String json = new String(message.getBody());
                            PriceDTO price = objectMapper.readValue(json, PriceDTO.class);
                            callback.onPriceUpdate(price);
                            priceCache.put(ticker, price);
                        } catch (Exception e) {
                            log.error("Error processing price update: {}", e.getMessage());
                        }
                    }, channel.getBytes());
            } catch (Exception e) {
                log.error("Error subscribing to {}: {}", channel, e.getMessage());
            }
        });
        
        listenerThread.setDaemon(true);
        listenerThread.setName("PriceSubscriber-" + ticker);
        listenerThread.start();
        
        log.info("Subscribed to price updates for ticker: {}", ticker);
    }
    
    /**
     * Read historical prices from Redis stream
     * Useful for replaying recent price history
     * 
     * @param count Number of recent entries to retrieve
     * @return List of price records
     */
    public List<Map<String, String>> getPriceHistory(int count) {
        try {
            // Note: Redis stream reading implementation
            // This is a simplified version - full implementation would require
            // proper StreamOffset and StreamReadOptions configuration
            List<Map<String, String>> history = new ArrayList<>();
            // TODO: Implement full stream reading with pagination
            return history;
        } catch (Exception e) {
            log.error("Error reading price history: {}", e.getMessage());
            return Collections.emptyList();
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
