package com.westlakers.leap_bff.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TopInvestorDTO {
    private Long investorId;
    private String firstName;
    private String lastName;
    private String clientSegment;
    private Long totalTrades;
    private BigDecimal totalVolume;
    private BigDecimal totalValue;
}