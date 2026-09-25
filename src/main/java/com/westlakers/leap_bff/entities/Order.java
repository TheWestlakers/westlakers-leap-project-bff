package com.westlakers.leap_bff.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long orderId;
    private Long accountId;
    private Long instrumentId;
    private String side;
    private String orderType;
    private BigDecimal limitPrice;
    private BigDecimal quantity;
    private Long statusId;
    private LocalDateTime placedAt;
    private LocalDateTime executedAt;
}
