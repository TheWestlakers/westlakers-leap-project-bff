package com.westlakers.leap_bff.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class PriceWebSocketHandler implements WebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final Map<WebSocketSession, Set<String>> subscriptions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile Thread priceStreamThread;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        subscriptions.put(session, ConcurrentHashMap.newKeySet());
        log.info("WebSocket connected. Total sessions: {}", sessions.size());
        
        // Send welcome message
        sendMessage(session, Map.of(
            "type", "connection_established",
            "message", "Connected to price WebSocket",
            "sessionId", session.getId(),
            "timestamp", System.currentTimeMillis()
        ));
        
        // Start price stream if not already running
        if (priceStreamThread == null || !priceStreamThread.isAlive()) {
            startPriceStream();
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            handleTextMessage(session, (TextMessage) message);
        }
    }

    private void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Received from {}: {}", session.getId(), payload);
        
        try {
            JsonNode node = objectMapper.readTree(payload);
            String action = node.get("action").asText("unknown");
            
            if ("subscribe".equals(action)) {
                handleSubscribe(session, node);
            } else if ("unsubscribe".equals(action)) {
                handleUnsubscribe(session, node);
            } else if ("ping".equals(action)) {
                sendMessage(session, Map.of(
                    "type", "pong",
                    "timestamp", System.currentTimeMillis()
                ));
            } else {
                sendMessage(session, Map.of(
                    "type", "error",
                    "message", "Unknown action: " + action
                ));
            }
        } catch (Exception e) {
            log.error("Error handling message: {}", e.getMessage(), e);
            sendMessage(session, Map.of(
                "type", "error",
                "message", e.getMessage()
            ));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sessions.remove(session);
        subscriptions.remove(session);
        log.info("WebSocket closed. Total sessions: {}", sessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for {}: {}", session.getId(), exception.getMessage(), exception);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void handleSubscribe(WebSocketSession session, JsonNode node) throws IOException {
        @SuppressWarnings("unchecked")
        List<String> tickers = (List<String>) objectMapper.convertValue(
            node.get("tickers"), 
            List.class
        );
        
        Set<String> subs = subscriptions.getOrDefault(session, ConcurrentHashMap.newKeySet());
        subs.addAll(tickers);
        subscriptions.put(session, subs);
        
        log.info("Subscribe request from {} for tickers: {}", session.getId(), tickers);
        
        sendMessage(session, Map.of(
            "type", "subscription_confirmed",
            "tickers", tickers,
            "count", tickers.size(),
            "timestamp", System.currentTimeMillis()
        ));
    }

    private void handleUnsubscribe(WebSocketSession session, JsonNode node) throws IOException {
        @SuppressWarnings("unchecked")
        List<String> tickers = (List<String>) objectMapper.convertValue(
            node.get("tickers"), 
            List.class
        );
        
        Set<String> subs = subscriptions.getOrDefault(session, ConcurrentHashMap.newKeySet());
        subs.removeAll(tickers);
        subscriptions.put(session, subs);
        
        log.info("Unsubscribe request from {} for tickers: {}", session.getId(), tickers);
        
        sendMessage(session, Map.of(
            "type", "unsubscription_confirmed",
            "tickers", tickers,
            "timestamp", System.currentTimeMillis()
        ));
    }

    private void sendMessage(WebSocketSession session, Object payload) throws IOException {
        if (session.isOpen()) {
            String json = objectMapper.writeValueAsString(payload);
            session.sendMessage(new TextMessage(json));
        }
    }

    private void startPriceStream() {
        priceStreamThread = new Thread(() -> {
            Map<String, Map<String, Object>> priceState = new ConcurrentHashMap<>();
            
            // Initialize base prices
            String[] tickers = {"AAPL", "MSFT", "GOOGL", "TSLA", "AMZN"};
            Map<String, Double> basePrices = Map.of(
                "AAPL", 182.50,
                "MSFT", 420.75,
                "GOOGL", 138.90,
                "TSLA", 245.30,
                "AMZN", 185.50
            );
            
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Send price updates to all subscribed sessions
                    for (WebSocketSession session : sessions) {
                        if (!session.isOpen()) continue;
                        
                        Set<String> subs = subscriptions.getOrDefault(session, new HashSet<>());
                        if (subs.isEmpty()) continue;
                        
                        // Send price update for each subscribed ticker
                        for (String ticker : subs) {
                            Map<String, Object> price = generateMockPrice(
                                ticker, 
                                basePrices.getOrDefault(ticker, 100.0),
                                priceState
                            );
                            
                            sendMessage(session, Map.of(
                                "type", "price_update",
                                "ticker", ticker,
                                "data", price,
                                "timestamp", System.currentTimeMillis()
                            ));
                        }
                    }
                    
                    Thread.sleep(500); // Send updates every 500ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Error in price stream: {}", e.getMessage());
                }
            }
        });
        
        priceStreamThread.setDaemon(true);
        priceStreamThread.setName("PriceStream");
        priceStreamThread.start();
    }

    private Map<String, Object> generateMockPrice(String ticker, Double basePrice, 
                                                    Map<String, Map<String, Object>> priceState) {
        Map<String, Object> state = priceState.getOrDefault(ticker, new ConcurrentHashMap<>());
        
        Double lastPrice = (Double) state.getOrDefault("lastPrice", basePrice);
        Double change = (Math.random() - 0.48) * 2; // Slight upward bias
        Double newPrice = Math.max(basePrice * 0.8, lastPrice + change);
        
        Double changePct = ((newPrice - basePrice) / basePrice) * 100;
        Double bid = newPrice - 0.05;
        Double ask = newPrice + 0.05;
        long volume = Math.round(Math.random() * 10000000);
        
        state.put("lastPrice", newPrice);
        priceState.put(ticker, state);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ticker", ticker);
        result.put("currentPrice", newPrice);
        result.put("open", basePrice);
        result.put("high", basePrice * 1.05);
        result.put("low", basePrice * 0.95);
        result.put("close", newPrice);
        result.put("volume", volume);
        result.put("bid", bid);
        result.put("ask", ask);
        result.put("change", newPrice - basePrice);
        result.put("changePct", changePct);
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
}
