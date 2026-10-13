package com.westlakers.leap_bff.config;

import com.westlakers.leap_bff.handlers.PriceWebSocketHandler;
import com.westlakers.leap_bff.services.PriceStreamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private PriceWebSocketHandler priceWebSocketHandler;

    @Bean
    public PriceWebSocketHandler priceWebSocketHandler(PriceStreamingService priceStreamingService) {
        return new PriceWebSocketHandler(priceStreamingService);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(priceWebSocketHandler, "/ws/prices")
                .setAllowedOrigins("*");
    }
}
