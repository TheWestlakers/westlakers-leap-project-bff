package com.westlakers.leap_bff.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import com.westlakers.leap_bff.entities.Holding;

/**
 * Lightweight DTO for Holding list responses.
 * Contains essential holding information for portfolio views.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldingDTO {
    private Long holdingId;
    private Long accountId;
    private Long instrumentId;
    private BigDecimal quantity;
    private BigDecimal averagePrice;

    /**
     * Factory method to convert Holding entity to HoldingDTO.
     */
    public static HoldingDTO fromEntity(Holding holding) {
        return HoldingDTO.builder()
                .holdingId(holding.getHoldingId())
                .accountId(holding.getAccountId())
                .instrumentId(holding.getInstrumentId())
                .quantity(holding.getQuantity())
                .averagePrice(holding.getAveragePrice())
                .build();
    }
}
