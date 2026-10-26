package com.westlakers.leap_bff.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.westlakers.leap_bff.dtos.NewsArticleDTO;
import com.westlakers.leap_bff.dtos.NewsResponseDTO;
import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class NewsService {
    private static final int PAGE_SIZE = 8;
    private static final int MARKET_PAGE_SIZE = 50;
    private static final int TICKER_PAGE_SIZE = 10;
    private static final Duration MARKET_CACHE_DURATION = Duration.ofMinutes(10);
    private static final Duration TICKER_CACHE_DURATION = Duration.ofMinutes(5);
    private static final String MARKET_CACHE_KEY = "market";
    private static final String DEFAULT_SECTOR = "stock market";
    private static final List<String> MARKET_TERMS = List.of(
            "stock", "stocks", "market", "markets", "economy", "economic", "earnings",
            "revenue", "profit", "finance", "financial", "invest", "investing", "investment",
            "trading", "trade", "shares", "equity", "bond", "treasury", "interest rate",
            "inflation", "federal reserve", "nasdaq", "dow", "s&p", "sp500", "oil", "gold",
            "crypto", "bitcoin", "forex", "ipo", "merger", "acquisition", "tariff", "bank"
    );

    private final RestClient newsApiClient;
    private final ObjectMapper objectMapper;
    private final String newsApiKey;
    private final ConcurrentMap<String, CachedHeadlines> marketCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CachedHeadlines> tickerCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, TickerSearch> requestedTickerQueries = new ConcurrentHashMap<>();

    public NewsService(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${news.api-key:}") String newsApiKey,
            @Value("${news.base-url:https://newsapi.org/v2}") String newsBaseUrl) {
        this.newsApiClient = restClientBuilder.clone().baseUrl(newsBaseUrl).build();
        this.objectMapper = objectMapper;
        this.newsApiKey = newsApiKey;
    }

    public NewsResponseDTO getMarketHeadlines() {
        CachedHeadlines cached = marketCache.get(MARKET_CACHE_KEY);
        if (isFresh(cached, MARKET_CACHE_DURATION)) {
            return cached.response();
        }

        if (!refreshMarketHeadlinesNow() && cached != null) {
            return cached.response();
        }
        if (marketCache.get(MARKET_CACHE_KEY) == null) {
            throw new ApiException(ErrorCode.NEWS_UNAVAILABLE, "Market news is temporarily unavailable");
        }
        return marketCache.get(MARKET_CACHE_KEY).response();
    }

    @Scheduled(
            fixedRateString = "${news.market-refresh-ms:600000}",
            initialDelayString = "${news.market-initial-delay-ms:0}")
    public void refreshMarketHeadlines() {
        refreshMarketHeadlinesNow();
    }

    public NewsResponseDTO getTickerHeadlines(String symbol, String companyName, String sector) {
        TickerSearch search = new TickerSearch(
                normalizeSymbol(symbol),
                normalizeSearchTerm(companyName),
                normalizeSearchTerm(sector));
        requestedTickerQueries.put(search.symbol(), search);

        CachedHeadlines cached = tickerCache.get(search.symbol());
        if (isFresh(cached, TICKER_CACHE_DURATION)) {
            return cached.response();
        }

        if (!refreshTickerHeadlinesNow(search) && cached != null) {
            return cached.response();
        }
        return tickerCache.getOrDefault(search.symbol(), new CachedHeadlines(emptyResponse(), Instant.now())).response();
    }

    @Scheduled(
            fixedRateString = "${news.ticker-refresh-ms:300000}",
            initialDelayString = "${news.ticker-initial-delay-ms:300000}")
    public void refreshTickerHeadlines() {
        requestedTickerQueries.values().forEach(this::refreshTickerHeadlinesNow);
    }

    private boolean refreshMarketHeadlinesNow() {
        if (newsApiKey.isBlank()) {
            log.warn("Market news refresh skipped because NEWS_API_KEY is not configured");
            return false;
        }

        try {
            NewsResponseDTO response = fetchNewsApi("/top-headlines", null, MARKET_PAGE_SIZE, true);
            marketCache.put(MARKET_CACHE_KEY, new CachedHeadlines(response, Instant.now()));
            log.info("Refreshed {} market news articles", response.articles().size());
            return true;
        } catch (RuntimeException exception) {
            log.warn("Market news refresh failed: {}", exception.getMessage());
            return false;
        }
    }

    private boolean refreshTickerHeadlinesNow(TickerSearch search) {
        if (newsApiKey.isBlank()) {
            log.warn("Ticker news refresh skipped because NEWS_API_KEY is not configured");
            return false;
        }

        try {
            String query = buildTickerQuery(search);
            NewsResponseDTO response = fetchNewsApi("/everything", query, TICKER_PAGE_SIZE, false);
            tickerCache.put(search.symbol(), new CachedHeadlines(response, Instant.now()));
            log.info("Refreshed {} ticker news articles for {} using query {}", response.articles().size(), search.symbol(), query);
            return true;
        } catch (RuntimeException exception) {
            log.warn("Ticker news refresh failed for {}: {}", search.symbol(), exception.getMessage());
            return false;
        }
    }

    private NewsResponseDTO fetchNewsApi(String path, String query, int pageSize, boolean marketFilter) {
        String responseBody = newsApiClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(path).queryParam("pageSize", pageSize);
                    if (marketFilter) {
                        uriBuilder.queryParam("country", "us")
                                .queryParam("category", "business");
                    } else {
                        uriBuilder.queryParam("language", "en")
                                .queryParam("sortBy", "publishedAt");
                    }
                    if (query != null && !query.isBlank()) {
                        uriBuilder.queryParam("q", query);
                    }
                    return uriBuilder.build();
                })
                .header("X-Api-Key", newsApiKey)
                .retrieve()
                .body(String.class);

        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException("NewsAPI returned an empty response");
        }

        try {
            return parseNewsApiResponse(responseBody, marketFilter);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("NewsAPI returned invalid JSON", exception);
        }
    }

    private NewsResponseDTO parseNewsApiResponse(String responseBody, boolean marketFilter)
            throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(responseBody);
        if (!"ok".equalsIgnoreCase(root.path("status").asText())) {
            throw new IllegalStateException("NewsAPI returned an unsuccessful status");
        }

        Stream<NewsArticleDTO> articles = StreamSupport.stream(root.path("articles").spliterator(), false)
                .map(this::toArticle)
                .filter(Objects::nonNull);
        if (marketFilter) {
            articles = articles.filter(this::isMarketRelevant);
        }

        List<NewsArticleDTO> limitedArticles = articles.limit(PAGE_SIZE).toList();
        return new NewsResponseDTO(limitedArticles.size(), limitedArticles);
    }

    private String buildTickerQuery(TickerSearch search) {
        String sector = search.sector() == null ? DEFAULT_SECTOR : search.sector();
        return Stream.of(search.symbol(), search.companyName(), sector)
                .map(this::normalizeSearchTerm)
                .filter(Objects::nonNull)
                .distinct()
                .map(term -> "\"" + term.replace("\"", "") + "\"")
                .reduce((left, right) -> left + " OR " + right)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INPUT, "A ticker search term is required"));
    }

    private boolean isMarketRelevant(NewsArticleDTO article) {
        String title = article.title().toLowerCase(Locale.ROOT);
        return MARKET_TERMS.stream().anyMatch(title::contains);
    }

    private NewsArticleDTO toArticle(JsonNode article) {
        String title = text(article, "title");
        if (title == null || "[Removed]".equalsIgnoreCase(title)) {
            return null;
        }

        return new NewsArticleDTO(
                text(article.path("source"), "name"),
                text(article, "author"),
                title,
                text(article, "description"),
                text(article, "url"),
                text(article, "urlToImage"),
                text(article, "publishedAt"));
    }

    private String text(JsonNode node, String field) {
        String value = node.path(field).asText("").trim();
        return value.isBlank() ? null : value;
    }

    private String normalizeSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Ticker cannot be empty");
        }

        String normalized = symbol.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9._=\\-]{1,20}")) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Invalid ticker format");
        }
        return normalized;
    }

    private String normalizeSearchTerm(String term) {
        if (term == null) {
            return null;
        }
        String normalized = term.trim().replaceAll("\\s+", " ");
        if (normalized.isBlank()) {
            return null;
        }
        return normalized.substring(0, Math.min(normalized.length(), 80));
    }

    private NewsResponseDTO emptyResponse() {
        return new NewsResponseDTO(0, List.of());
    }

    private boolean isFresh(CachedHeadlines cached, Duration maxAge) {
        return cached != null && cached.fetchedAt().plus(maxAge).isAfter(Instant.now());
    }

    private record CachedHeadlines(NewsResponseDTO response, Instant fetchedAt) {
    }

    private record TickerSearch(String symbol, String companyName, String sector) {
    }
}
