package com.westlakers.leap_bff.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.westlakers.leap_bff.dtos.HoldingDTO;
import com.westlakers.leap_bff.entities.Holding;
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

    @GetMapping("/holdings/{id}/validate")
    public ResponseEntity<String> validateHoldingExists(@PathVariable Long id) {
        try {
            this.holdingService.getHoldingById(id);
            return ResponseEntity.ok()
                .header("X-Holding-Status", "VALID")
                .body("Holding exists and is valid");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Holding not found with id: " + id);
        }
    }

    @GetMapping("/holdings/{id}/details")
    public ResponseEntity<String> checkHoldingDetails(@PathVariable Long id) {
        try {
            HoldingDTO holding = this.holdingService.getHoldingById(id);
            return ResponseEntity.ok()
                .body("Holding quantity: " + holding.getQuantity() + ", Average price: " + holding.getAveragePrice());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body("Unable to retrieve holding details: " + e.getMessage());
        }
    }

    @PostMapping("/holdings")
    public ResponseEntity<String> createHolding(@RequestBody Holding holding) {
        try {
            HoldingDTO createdHolding = this.holdingService.createHolding(holding);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body("Holding created successfully with ID: " + createdHolding.getHoldingId());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body("Failed to create holding: " + e.getMessage());
        }
    }
}