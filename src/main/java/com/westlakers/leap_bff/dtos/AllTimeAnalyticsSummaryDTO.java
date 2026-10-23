package com.westlakers.leap_bff.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AllTimeAnalyticsSummaryDTO {
    private BigDecimal averageTradeSize;
    private Long instrumentsTraded;
    private BigDecimal averageInstrumentsPerActiveClient;
    private Long operatingTraders;
    private BigDecimal totalAbsoluteFundsProcessed;
}
