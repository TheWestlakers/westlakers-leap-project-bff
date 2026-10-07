package com.westlakers.leap_bff.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeExecutionResponse {
    
    private Long orderId;
    private Long accountId;
    private Long instrumentId;
    private String side;
    private String orderType;
    private BigDecimal quantity;
    private BigDecimal executionPrice;
    private BigDecimal limitPrice;
    private BigDecimal totalValue;
    private String status;
    private LocalDateTime placedAt;
    private LocalDateTime executedAt;
    private String message;
}
