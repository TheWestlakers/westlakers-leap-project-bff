package com.westlakers.leap_bff.entities;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Market {
    private Long marketId;
    private String marketName;
    private String marketCode;
    private String timezone;
    private LocalTime openTime;
    private LocalTime closeTime;
}