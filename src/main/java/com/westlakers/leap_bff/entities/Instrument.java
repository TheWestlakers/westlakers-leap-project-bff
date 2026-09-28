package com.westlakers.leap_bff.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class Instrument {
    private Long instrumentId;
    private Long marketId;
    private Long assetClassId;
    private String ticker;
    private String name;
}
