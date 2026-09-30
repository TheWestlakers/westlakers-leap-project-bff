package com.westlakers.leap_bff.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private Long accountId;
    
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be a positive number")
    private Long userId;
    
    @NotNull(message = "Account type ID is required")
    @Positive(message = "Account type ID must be a positive number")
    private Long accountTypeId;
    
    @Positive(message = "Account status ID must be a positive number")
    private Long accountStatusId;
    
    @PastOrPresent(message = "Created date cannot be in the future")
    private LocalDateTime createdAt;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code (e.g., USD, EUR, GBP)")
    private String currency;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Settled cash cannot be negative")
    private BigDecimal settledCash;
}
