package com.westlakers.leap_bff.services;

import com.westlakers.leap_bff.dtos.PriceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for retrieving live stock prices from Redis cache.
 * Prices are populated by the analytics/streaming pipeline.
 */
@Service
@Slf4j
public class PriceService {

    private final RedisTemplate<String, PriceDTO> priceRedisTemplate;
    private static final String PRICE_KEY_PREFIX = "price:";

    public PriceService(RedisTemplate<String, PriceDTO> priceRedisTemplate) {
        this.priceRedisTemplate = priceRedisTemplate;
    }

    /**
     * Get price for a single symbol from Redis cache.
     *
     * @param symbol Stock ticker symbol (e.g., "AAPL")
     * @return PriceDTO if found, null otherwise
     */
    public PriceDTO getPrice(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return null;
        }

        try {
            String key = PRICE_KEY_PREFIX + symbol.toUpperCase();
            PriceDTO price = priceRedisTemplate.opsForValue().get(key);
            
            if (price != null) {
                log.debug("Retrieved price for {}: ${}", symbol, price.getCurrentPrice());
            } else {
                log.debug("No cached price found for symbol: {}", symbol);
            }
            
            return price;
        } catch (Exception e) {
            log.error("Error retrieving price for symbol {}: {}", symbol, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get prices for multiple symbols.
     *
     * @param symbols List of stock ticker symbols
     * @return Map of symbol -> PriceDTO
     */
    public Map<String, PriceDTO> getPrices(List<String> symbols) {
        Map<String, PriceDTO> result = new LinkedHashMap<>();

        if (symbols == null || symbols.isEmpty()) {
            return result;
        }

        try {
            for (String symbol : symbols) {
                PriceDTO price = getPrice(symbol);
                if (price != null) {
                    result.put(symbol.toUpperCase(), price);
                }
            }
            
            log.debug("Retrieved {} prices for {} requested symbols", result.size(), symbols.size());
            return result;
        } catch (Exception e) {
            log.error("Error retrieving multiple prices: {}", e.getMessage(), e);
            return result;
        }
    }

    /**
     * Get all available prices from Redis cache.
     *
     * @return Map of all cached prices (symbol -> PriceDTO)
     */
    public Map<String, PriceDTO> getAllPrices() {
        Map<String, PriceDTO> result = new LinkedHashMap<>();

        try {
            Set<String> keys = priceRedisTemplate.keys(PRICE_KEY_PREFIX + "*");
            
            if (keys == null || keys.isEmpty()) {
                log.warn("No prices cached in Redis");
                return result;
            }

            for (String key : keys) {
                PriceDTO price = priceRedisTemplate.opsForValue().get(key);
                if (price != null) {
                    result.put(price.getSymbol(), price);
                }
            }
            
            log.debug("Retrieved {} prices from cache", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error retrieving all prices: {}", e.getMessage(), e);
            return result;
        }
    }

    /**
     * Check if a price exists in cache.
     *
     * @param symbol Stock ticker symbol
     * @return true if price is cached, false otherwise
     */
    public boolean priceExists(String symbol) {
        try {
            String key = PRICE_KEY_PREFIX + symbol.toUpperCase();
            return Boolean.TRUE.equals(priceRedisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Error checking if price exists: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get cache statistics.
     *
     * @return Map with cache stats
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        try {
            Set<String> keys = priceRedisTemplate.keys(PRICE_KEY_PREFIX + "*");
            stats.put("cachedSymbols", keys != null ? keys.size() : 0);
            stats.put("lastUpdated", new Date());
            
            if (keys != null && !keys.isEmpty()) {
                // Get a sample price's timestamp
                PriceDTO sample = priceRedisTemplate.opsForValue().get(keys.iterator().next());
                if (sample != null && sample.getLastUpdate() != null) {
                    stats.put("sampleLastUpdate", sample.getLastUpdate());
                }
            }
            
            return stats;
        } catch (Exception e) {
            log.error("Error retrieving cache stats: {}", e.getMessage());
            stats.put("error", "Failed to retrieve stats");
            return stats;
        }
    }
}
