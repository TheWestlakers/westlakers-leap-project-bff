package com.westlakers.leap_bff.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NewsServiceTest {
    private static final String NEWS_RESPONSE = """
            {"status":"ok","totalResults":1,"articles":[{"source":{"name":"Market Desk"},"author":"Reporter","title":"Apple earnings lift markets","description":"A market update","url":"https://example.com/news","urlToImage":null,"publishedAt":"2026-10-26T15:00:00Z"}]}
            """;

    private final List<URI> requests = new CopyOnWriteArrayList<>();
    private HttpServer server;
    private NewsService newsService;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/top-headlines", exchange -> {
            requests.add(exchange.getRequestURI());
            respond(exchange, NEWS_RESPONSE);
        });
        server.createContext("/everything", exchange -> {
            requests.add(exchange.getRequestURI());
            respond(exchange, NEWS_RESPONSE);
        });
        server.start();

        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        newsService = new NewsService(RestClient.builder(), new ObjectMapper(), "test-key", baseUrl);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void requestsBusinessMarketHeadlines() {
        assertEquals(1, newsService.getMarketHeadlines().articles().size());

        String query = URLDecoder.decode(requests.get(0).getQuery(), StandardCharsets.UTF_8);
        assertTrue(query.contains("category=business"));
        assertTrue(query.contains("pageSize=50"));
    }

    @Test
    void requestsTickerCompanySectorAndSymbolTerms() {
        assertEquals(1, newsService.getTickerHeadlines("AAPL", "Apple Inc.", "Technology").articles().size());

        assertEquals("/everything", requests.get(0).getPath());
        String query = URLDecoder.decode(requests.get(0).getQuery(), StandardCharsets.UTF_8);
        assertTrue(query.contains("language=en"));
        assertTrue(query.contains("sortBy=publishedAt"));
        assertTrue(query.contains("pageSize=10"));
        assertTrue(query.contains("AAPL"));
        assertTrue(query.contains("Apple Inc."));
        assertTrue(query.contains("Technology"));
    }

    @Test
    void declaresTenAndFiveMinuteRefreshIntervals() throws NoSuchMethodException {
        Scheduled marketSchedule = NewsService.class
                .getMethod("refreshMarketHeadlines")
                .getAnnotation(Scheduled.class);
        Scheduled tickerSchedule = NewsService.class
                .getMethod("refreshTickerHeadlines")
                .getAnnotation(Scheduled.class);

        assertEquals("${news.market-refresh-ms:600000}", marketSchedule.fixedRateString());
        assertEquals("${news.ticker-refresh-ms:300000}", tickerSchedule.fixedRateString());
    }

    private void respond(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
