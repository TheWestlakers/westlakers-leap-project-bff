package com.westlakers.leap_bff.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long orderId;
    
    @NotNull(message = "Account ID cannot be null")
    @Positive(message = "Account ID must be positive")
    private Long accountId;
    
    @NotNull(message = "Instrument ID cannot be null")
    @Positive(message = "Instrument ID must be positive")
    private Long instrumentId;
    
    @NotBlank(message = "Side cannot be blank")
    private String side;
    
    @NotBlank(message = "Order type cannot be blank")
    private String orderType;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Limit price must be greater than 0")
    private BigDecimal limitPrice;
    
    @NotNull(message = "Quantity cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantity;
    
    private Long status;
    private LocalDateTime placedAt;
    private LocalDateTime executedAt;
}
