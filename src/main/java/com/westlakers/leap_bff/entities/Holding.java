package com.westlakers.leap_bff.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Holding {
    private Long holdingId;
    private Long accountId;
    private Long instrumentId;
    private BigDecimal quantity;
    private BigDecimal averagePrice;
}
