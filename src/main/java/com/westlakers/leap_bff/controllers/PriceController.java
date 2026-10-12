package com.westlakers.leap_bff.controllers;

import com.westlakers.leap_bff.dtos.PriceDTO;
import com.westlakers.leap_bff.services.PriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for live price data endpoints.
 * Provides access to cached stock prices from Redis.
 * 
 * Endpoints:
 * - GET /api/v1/prices/{symbol} - Get price for single symbol
 * - GET /api/v1/prices - Get prices (all or filtered by symbols query param)
 * - GET /api/v1/prices/cache/stats - Get cache statistics
 */
@RestController
@RequestMapping("/api/v1/prices")
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://10.14.134.216:4200"})
public class PriceController {
    
    private final PriceService priceService;
    
    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }
    
    /**
     * Get price for a single symbol.
     * 
     * @param symbol Stock ticker symbol (e.g., "AAPL")
     * @return PriceDTO with current price data
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<?> getPrice(@PathVariable String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Symbol cannot be empty"));
        }
        
        try {
            PriceDTO price = priceService.getPrice(symbol);
            
            if (price == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Price not found for symbol: " + symbol, "symbol", symbol.toUpperCase()));
            }
            
            return ResponseEntity.ok(price);
            
        } catch (Exception e) {
            log.error("Error retrieving price for symbol {}: {}", symbol, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve price"));
        }
    }
    
    /**
     * Get prices for multiple symbols or all prices.
     * Pass symbols as query parameter: ?symbols=AAPL,MSFT,GOOGL
     * If no symbols specified, returns all cached prices.
     * 
     * @param symbols Comma-separated list of symbols (optional)
     * @return Map of symbol -> PriceDTO
     */
    @GetMapping
    public ResponseEntity<?> getPrices(@RequestParam(value = "symbols", required = false) String symbols) {
        try {
            Map<String, PriceDTO> prices;
            
            if (symbols == null || symbols.trim().isEmpty()) {
                // Return all cached prices
                prices = priceService.getAllPrices();
                log.debug("Returning all cached prices");
            } else {
                // Parse symbols and fetch specific ones
                String[] symbolArray = symbols.split(",");
                List<String> symbolList = java.util.Arrays.asList(symbolArray);
                prices = priceService.getPrices(symbolList);
                log.debug("Returning {} prices for requested symbols", prices.size());
            }
            
            if (prices.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No prices found for requested symbols", "requestedSymbols", symbols));
            }
            
            return ResponseEntity.ok(prices);
            
        } catch (Exception e) {
            log.error("Error retrieving prices: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve prices"));
        }
    }
    
    /**
     * Get cache statistics and health information.
     * 
     * @return Map with cache stats
     */
    @GetMapping("/cache/stats")
    public ResponseEntity<?> getCacheStats() {
        try {
            Map<String, Object> stats = priceService.getCacheStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error retrieving cache stats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve cache statistics"));
        }
    }
}
