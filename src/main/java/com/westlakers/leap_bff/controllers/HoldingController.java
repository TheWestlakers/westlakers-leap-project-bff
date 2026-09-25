package com.westlakers.leap_bff.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.westlakers.leap_bff.dtos.HoldingDTO;
import com.westlakers.leap_bff.services.HoldingService;


@RestController 
@RequestMapping("/api")
public class HoldingController {
    private final HoldingService holdingService;

    public HoldingController(HoldingService holdingService) {
        this.holdingService = holdingService;
    }

    @GetMapping("/holdings")
    public List<HoldingDTO> getAllHoldings() {
        return this.holdingService.getAllHoldings();
    }

    @GetMapping("/holdings/{id}")
    public HoldingDTO getHoldingById(@PathVariable Long id) {
        return this.holdingService.getHoldingById(id);
    }

    @GetMapping("/accounts/{accountId}/holdings")
    public List<HoldingDTO> getHoldingsByAccountId(@PathVariable Long accountId) {
        return this.holdingService.getHoldingsByAccountId(accountId);
    }
}