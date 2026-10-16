package com.westlakers.leap_bff.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.westlakers.leap_bff.dtos.TradingVolumePageDTO;
import com.westlakers.leap_bff.services.TradingVolumeService;

@RestController
@RequestMapping("/api/trading-volumes")
public class TradingVolumeController {

    private final TradingVolumeService tradingVolumeService;

    public TradingVolumeController(TradingVolumeService tradingVolumeService) {
        this.tradingVolumeService = tradingVolumeService;
    }

    @GetMapping
    public ResponseEntity<TradingVolumePageDTO> getAllTradingVolumes(
            @RequestParam(defaultValue = "0") int page) {
        if (page < 0) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(tradingVolumeService.getTradingVolumes(page));
    }
}