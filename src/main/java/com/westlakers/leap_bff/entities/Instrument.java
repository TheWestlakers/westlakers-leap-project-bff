package com.westlakers.leap_bff.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class Instrument {
    @Positive(message = "Instrument ID must be a positive number")
    private Long instrumentId;
    
    @NotNull(message = "Market ID is required")
    @Positive(message = "Market ID must be a positive number")
    private Long marketId;
    
    @NotNull(message = "Asset Class ID is required")
    @Positive(message = "Asset Class ID must be a positive number")
    private Long assetClassId;
    
    @NotBlank(message = "Ticker is required and cannot be empty")
    @Size(min = 1, max = 20, message = "Ticker must be between 1 and 20 characters")
    private String ticker;
    
    @NotBlank(message = "Name is required and cannot be empty")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;
}
