package com.westlakers.leap_bff.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for live stock price data.
 * Contains current price and OHLCV (Open, High, Low, Close, Volume) information.
 * Used for Redis stream serialization and REST API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceDTO {
    
    @JsonProperty("symbol")
    private String symbol;
    
    @JsonProperty("currentPrice")
    private BigDecimal currentPrice;
    
    @JsonProperty("open")
    private BigDecimal open;
    
    @JsonProperty("high")
    private BigDecimal high;
    
    @JsonProperty("low")
    private BigDecimal low;
    
    @JsonProperty("close")
    private BigDecimal close;
    
    @JsonProperty("volume")
    private Long volume;
    
    @JsonProperty("bid")
    private BigDecimal bid;
    
    @JsonProperty("ask")
    private BigDecimal ask;
    
    @JsonProperty("change")
    private BigDecimal change;
    
    @JsonProperty("changePercent")
    private BigDecimal changePercent;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("lastUpdate")
    private LocalDateTime lastUpdate;
}
