package com.westlakers.leap_bff.controllers;

import com.westlakers.leap_bff.dtos.PriceDTO;
import com.westlakers.leap_bff.services.PriceStreamingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST Controller for real-time price data streaming.
 * 
 * Endpoints:
 * - GET /api/prices/latest/{ticker} - Get latest price for a ticker
 * - GET /api/prices/all - Get prices for all active tickers
 * - GET /api/prices/dashboard - Get dashboard with all price data
 * - GET /api/prices/stats/{ticker} - Get detailed statistics for a ticker
 * - GET /api/prices/tickers - Get list of available tickers being streamed
 * - GET /api/prices/stream-status - Check if streaming is active
 * 
 * Example Usage:
 * curl http://localhost:8080/api/prices/latest/AAPL
 * curl http://localhost:8080/api/prices/dashboard
 */
@Slf4j
@RestController
@RequestMapping("/api/prices")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PriceStreamingController {
    
    @Autowired
    private PriceStreamingService priceStreamingService;
    
    /**
     * Get the latest price for a specific ticker
     * 
     * @param ticker Stock ticker symbol (e.g., AAPL, MSFT)
     * @return Latest PriceDTO with all price information
     */
    @GetMapping("/latest/{ticker}")
    public ResponseEntity<?> getLatestPrice(@PathVariable String ticker) {
        try {
            String registeredTicker = priceStreamingService.registerTicker(ticker);
            Optional<PriceDTO> price = priceStreamingService.getLatestPrice(registeredTicker);
            
            if (price.isPresent()) {
                return ResponseEntity.ok(price.get());
            } else {
                return ResponseEntity.ok(Map.of(
                    "ticker", ticker.toUpperCase(),
                    "status", "No data available",
                    "timestamp", new Date().toString()
                ));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid ticker",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error fetching latest price for {}: {}", ticker, e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to fetch price",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get latest prices for all active tickers
     * 
     * @return Map of ticker -> PriceDTO
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllPrices(@RequestParam(value = "symbols", required = false) List<String> symbols) {
        try {
            Set<String> tickers;

            if (symbols != null && !symbols.isEmpty()) {
                tickers = symbols.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(symbol -> !symbol.isEmpty())
                    .map(String::toUpperCase)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            } else {
                tickers = priceStreamingService.getAvailableTickers();
            }
            
            if (tickers.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "status", "No active tickers",
                    "message", "Live price streaming may not be running",
                    "hint", "Start the Python analytics service: python analytics/live_price_streaming.py"
                ));
            }
            
            Map<String, PriceDTO> prices = priceStreamingService.getLatestPrices(
                new ArrayList<>(tickers)
            );
            
            return ResponseEntity.ok(Map.of(
                "timestamp", new Date().toString(),
                "count", prices.size(),
                "prices", prices
            ));
        } catch (Exception e) {
            log.error("Error fetching all prices: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to fetch prices",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/tickers/register")
    public ResponseEntity<?> registerTickers(@RequestBody Map<String, Object> payload) {
        try {
            Set<String> registered = new LinkedHashSet<>();

            Object singleTicker = payload.get("ticker");
            if (singleTicker instanceof String) {
                String ticker = priceStreamingService.registerTicker((String) singleTicker);
                if (ticker != null) {
                    registered.add(ticker);
                }
            }

            Object symbols = payload.get("tickers");
            if (symbols instanceof List<?>) {
                List<String> tickerList = ((List<?>) symbols).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.toList());
                registered.addAll(priceStreamingService.registerTickers(tickerList));
            }

            return ResponseEntity.ok(Map.of(
                "count", registered.size(),
                "tickers", new ArrayList<>(registered)
            ));
        } catch (Exception e) {
            log.error("Error registering tickers: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to register tickers",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/tickers/unregister")
    public ResponseEntity<?> unregisterTicker(@RequestBody Map<String, String> payload) {
        try {
            String ticker = payload.get("ticker");
            if (ticker == null || ticker.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Ticker is required"
                ));
            }

            String removed = priceStreamingService.unregisterTicker(ticker);
            return ResponseEntity.ok(Map.of("ticker", removed, "status", "removed"));
        } catch (Exception e) {
            log.error("Error unregistering ticker: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to unregister ticker",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/tickers/watchlist/sync")
    public ResponseEntity<?> syncWatchlist(@RequestBody Map<String, Object> payload) {
        try {
            Object clientIdValue = payload.get("clientId");
            if (!(clientIdValue instanceof String clientId) || clientId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "clientId is required"
                ));
            }

            Object symbols = payload.get("tickers");
            List<String> tickerList = new ArrayList<>();
            if (symbols instanceof List<?>) {
                tickerList = ((List<?>) symbols).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.toList());
            }

            Set<String> synced = priceStreamingService.syncWatchlist(clientId, tickerList);
            return ResponseEntity.ok(Map.of(
                "count", synced.size(),
                "tickers", new ArrayList<>(synced)
            ));
        } catch (Exception e) {
            log.error("Error syncing watchlist: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to sync watchlist",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/tickers/focus")
    public ResponseEntity<?> focusTicker(@RequestBody Map<String, String> payload) {
        try {
            String clientId = payload.get("clientId");
            String ticker = payload.get("ticker");
            if (clientId == null || clientId.trim().isEmpty() || ticker == null || ticker.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "clientId and ticker are required"
                ));
            }

            String focused = priceStreamingService.focusTicker(clientId, ticker);
            return ResponseEntity.ok(Map.of("ticker", focused, "status", "focused"));
        } catch (Exception e) {
            log.error("Error focusing ticker: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to focus ticker",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/tickers/unfocus")
    public ResponseEntity<?> unfocusTicker(@RequestBody Map<String, String> payload) {
        try {
            String clientId = payload.get("clientId");
            String ticker = payload.get("ticker");
            if (clientId == null || clientId.trim().isEmpty() || ticker == null || ticker.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "clientId and ticker are required"
                ));
            }

            String unfocused = priceStreamingService.unfocusTicker(clientId, ticker);
            return ResponseEntity.ok(Map.of("ticker", unfocused, "status", "unfocused"));
        } catch (Exception e) {
            log.error("Error unfocusing ticker: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to unfocus ticker",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/tickers/watchlist")
    public ResponseEntity<?> getClientWatchlist(@RequestParam("clientId") String clientId) {
        try {
            Set<String> tickers = priceStreamingService.getClientWatchlist(clientId);
            return ResponseEntity.ok(Map.of(
                "count", tickers.size(),
                "tickers", new ArrayList<>(tickers)
            ));
        } catch (Exception e) {
            log.error("Error fetching client watchlist: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to fetch watchlist",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get comprehensive dashboard with all price data and statistics
     * 
     * @return Dashboard data including streaming status and all prices
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        try {
            Map<String, Object> dashboard = priceStreamingService.getDashboardData();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Error fetching dashboard data: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to fetch dashboard",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get detailed statistics for a specific ticker
     * 
     * @param ticker Stock ticker symbol
     * @return Detailed price statistics including bid/ask, volume, market cap, etc.
     */
    @GetMapping("/stats/{ticker}")
    public ResponseEntity<?> getPriceStats(@PathVariable String ticker) {
        try {
            Map<String, Object> stats = priceStreamingService.getPriceStats(ticker);
            
            if (stats.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "ticker", ticker.toUpperCase(),
                    "status", "No data available",
                    "hint", "Ensure live_price_streaming.py is running and includes this ticker"
                ));
            }
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("ticker", ticker.toUpperCase());
            response.put("timestamp", new Date().toString());
            response.putAll(stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching stats for {}: {}", ticker, e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to fetch stats",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get list of all available tickers being streamed
     * 
     * @return Set of active ticker symbols
     */
    @GetMapping("/tickers")
    public ResponseEntity<?> getAvailableTickers() {
        try {
            Set<String> tickers = priceStreamingService.getAvailableTickers();
            
            return ResponseEntity.ok(Map.of(
                "timestamp", new Date().toString(),
                "count", tickers.size(),
                "tickers", new ArrayList<>(tickers),
                "streaming", priceStreamingService.isStreamingActive()
            ));
        } catch (Exception e) {
            log.error("Error fetching available tickers: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to fetch tickers",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Check if the price streaming service is active and healthy
     * 
     * @return Streaming status and health information
     */
    @GetMapping("/stream-status")
    public ResponseEntity<?> getStreamStatus() {
        try {
            boolean isActive = priceStreamingService.isStreamingActive();
            Set<String> tickers = priceStreamingService.getAvailableTickers();
            
            Map<String, Object> status = new LinkedHashMap<>();
            status.put("timestamp", new Date().toString());
            status.put("streaming", isActive);
            status.put("activeTickers", tickers.size());
            status.put("tickers", new ArrayList<>(tickers));
            
            if (!isActive) {
                status.put("hint", "Start live price streaming: cd analytics && python live_price_streaming.py");
            }
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error checking stream status: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to check stream status",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get price history from Redis stream (for replay/analysis)
     * 
     * @param limit Maximum number of historical records to retrieve (default: 10)
     * @return List of recent price records
     */
    @GetMapping("/history")
    public ResponseEntity<?> getPriceHistory(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, String>> history = priceStreamingService.getPriceHistory(limit);
            
            return ResponseEntity.ok(Map.of(
                "timestamp", new Date().toString(),
                "limit", limit,
                "count", history.size(),
                "history", history
            ));
        } catch (Exception e) {
            log.error("Error fetching price history: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to fetch history",
                "message", e.getMessage()
            ));
        }
    }
}
