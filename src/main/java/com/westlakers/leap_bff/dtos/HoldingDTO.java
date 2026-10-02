package com.westlakers.leap_bff.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;

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
    @NotNull(message = "Holding ID cannot be null")
    @Positive(message = "Holding ID must be positive")
    private Long holdingId;
    
    @NotNull(message = "Account ID cannot be null")
    @Positive(message = "Account ID must be positive")
    private Long accountId;
    
    @NotNull(message = "Instrument ID cannot be null")
    @Positive(message = "Instrument ID must be positive")
    private Long instrumentId;
    
    @NotNull(message = "Quantity cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantity;
    
    @NotNull(message = "Average price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Average price must be greater than 0")
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
