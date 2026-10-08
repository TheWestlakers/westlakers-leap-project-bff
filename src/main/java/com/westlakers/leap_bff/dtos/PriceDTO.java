package com.westlakers.leap_bff.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Data Transfer Object for live stock price data.
 * Contains current price, OHLCV (Open, High, Low, Close, Volume) information,
 * and extended market data from real-time streaming.
 * Used for Redis stream serialization and REST API responses.
 * 
 * Supports both legacy format (symbol) and new streaming format (ticker).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceDTO {
    
    // Ticker identifier (used by streaming service)
    @JsonProperty("ticker")
    private String ticker;
    
    // Legacy symbol field (kept for backward compatibility)
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
    
    @JsonProperty("changePct")
    private BigDecimal changePct;
    
    @JsonProperty("changePercent")
    private BigDecimal changePercent;
    
    // Extended market data
    @JsonProperty("marketCap")
    private Long marketCap;
    
    @JsonProperty("peRatio")
    private BigDecimal peRatio;
    
    @JsonProperty("fiftyTwoWeekHigh")
    private BigDecimal fiftyTwoWeekHigh;
    
    @JsonProperty("fiftyTwoWeekLow")
    private BigDecimal fiftyTwoWeekLow;
    
    @JsonProperty("previousClose")
    private BigDecimal previousClose;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("lastUpdate")
    private String lastUpdate;
    
    @JsonProperty("exchange")
    private String exchange;
    
    @JsonProperty("currency")
    private String currency;
}
