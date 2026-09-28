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
public class Account {
    private Long accountId;
    private Long userId;
    private Long accountTypeId;
    private Long accountStatusId;
    private LocalDateTime createdAt;
    private String currency;
    private BigDecimal settledCash;
}
