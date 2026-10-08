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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("WebSocket connected. Total sessions: {}", sessions.size());
        
        // Send welcome message
        sendMessage(session, Map.of(
            "type", "connection_established",
            "message", "Connected to price WebSocket",
            "sessionId", session.getId(),
            "timestamp", System.currentTimeMillis()
        ));
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
        List<String> tickers = objectMapper.convertValue(
            node.get("tickers"), 
            List.class
        );
        
        log.info("Subscribe request from {} for tickers: {}", session.getId(), tickers);
        
        sendMessage(session, Map.of(
            "type", "subscription_confirmed",
            "tickers", tickers,
            "count", tickers.size(),
            "timestamp", System.currentTimeMillis()
        ));
    }

    private void handleUnsubscribe(WebSocketSession session, JsonNode node) throws IOException {
        List<String> tickers = objectMapper.convertValue(
            node.get("tickers"), 
            List.class
        );
        
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
}
