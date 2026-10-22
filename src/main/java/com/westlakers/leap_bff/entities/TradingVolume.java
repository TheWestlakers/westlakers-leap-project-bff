package com.westlakers.leap_bff.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradingVolume {
    private Long stagingId;
    private Long orderId;
    private String orderStatusId;
    private String orderStatus;
    private Long accountId;
    private Integer instrumentId;
    private Long userId;
    private String side;
    private BigDecimal quantity;
    private BigDecimal limitPrice;
    private BigDecimal executionPrice;
    private LocalDateTime executedAt;
    private String accountType;
    private String firstName;
    private String lastName;
    private String instrumentCode;
    private String instrumentName;
    private String instrumentType;
    private String clientSegment;
    private LocalDateTime createdAt;
}