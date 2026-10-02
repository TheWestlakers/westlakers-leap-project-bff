package com.westlakers.leap_bff.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Holding {
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
}
