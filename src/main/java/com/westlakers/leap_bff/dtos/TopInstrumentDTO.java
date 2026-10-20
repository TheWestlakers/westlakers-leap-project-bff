package com.westlakers.leap_bff.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TopInstrumentDTO {
    private Integer instrumentId;
    private String instrumentCode;
    private String instrumentName;
    private String instrumentType;
    private Long totalTrades;
    private BigDecimal totalVolume;
    private BigDecimal totalValue;
}