package com.westlakers.leap_bff.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LimitOrderRequest {
    
    @NotNull(message = "Account ID cannot be null")
    @Positive(message = "Account ID must be positive")
    private Long accountId;
    
    @NotNull(message = "Instrument ID cannot be null")
    @Positive(message = "Instrument ID must be positive")
    private Long instrumentId;
    
    @NotBlank(message = "Side cannot be blank")
    @Pattern(regexp = "^(BUY|SELL)$", message = "Side must be either BUY or SELL")
    private String side;
    
    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be greater than 0")
    private BigDecimal quantity;
    
    @NotNull(message = "Limit price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Limit price must be greater than 0")
    private BigDecimal limitPrice;
}
