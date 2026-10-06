package com.westlakers.leap_bff.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeExecutionRequest {
    
    @NotNull(message = "Execution price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Execution price must be greater than 0")
    private BigDecimal executionPrice;
}
