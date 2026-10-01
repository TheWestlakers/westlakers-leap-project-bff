package com.westlakers.leap_bff.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

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
    public ResponseEntity<HoldingDTO> validateHoldingExists(@PathVariable Long id) {
        HoldingDTO holding = this.holdingService.getHoldingById(id);
        return ResponseEntity.ok()
            .header("X-Holding-Status", "VALID")
            .body(holding);
    }

    @GetMapping("/holdings/{id}/details")
    public ResponseEntity<HoldingDTO> checkHoldingDetails(@PathVariable Long id) {
        HoldingDTO holding = this.holdingService.getHoldingById(id);
        return ResponseEntity.ok(holding);
    }

    @PostMapping("/holdings")
    public ResponseEntity<HoldingDTO> createHolding(@Valid @RequestBody Holding holding) {
        HoldingDTO createdHolding = this.holdingService.createHolding(holding);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdHolding);
    }

    @PatchMapping("/holdings/{id}")
    public ResponseEntity<HoldingDTO> updateHolding(@PathVariable Long id, @Valid @RequestBody Holding holding) {
        HoldingDTO updatedHolding = this.holdingService.updateHolding(id, holding);
        return ResponseEntity.ok(updatedHolding);
    }

    @DeleteMapping("/holdings/{id}")
    public ResponseEntity<Void> deleteHolding(@PathVariable Long id) {
        this.holdingService.deleteHolding(id);
        return ResponseEntity.noContent().build();
    }
}