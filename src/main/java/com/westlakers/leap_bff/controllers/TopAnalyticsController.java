package com.westlakers.leap_bff.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.westlakers.leap_bff.dtos.TopAnalyticsRequest;
import com.westlakers.leap_bff.dtos.TopInstrumentDTO;
import com.westlakers.leap_bff.dtos.TopInvestorDTO;
import com.westlakers.leap_bff.services.TopAnalyticsService;

@RestController
@RequestMapping("/api/analytics")
public class TopAnalyticsController {

    private final TopAnalyticsService topAnalyticsService;

    public TopAnalyticsController(TopAnalyticsService topAnalyticsService) {
        this.topAnalyticsService = topAnalyticsService;
    }

    @GetMapping("/top-investors")
    public ResponseEntity<List<TopInvestorDTO>> getTopInvestors(
            @Valid @ModelAttribute TopAnalyticsRequest request) {
        return ResponseEntity.ok(topAnalyticsService.getTopInvestors(request.getLimit()));
    }

    @GetMapping("/top-instruments")
    public ResponseEntity<List<TopInstrumentDTO>> getTopInstruments(
            @Valid @ModelAttribute TopAnalyticsRequest request) {
        return ResponseEntity.ok(topAnalyticsService.getTopInstruments(request.getLimit()));
    }
}
