package com.westlakers.leap_bff.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TradingVolumePageDTO {
    private List<TradingVolumeDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}