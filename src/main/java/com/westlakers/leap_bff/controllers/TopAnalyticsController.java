package com.westlakers.leap_bff.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.westlakers.leap_bff.dtos.RankingPageDTO;
import com.westlakers.leap_bff.dtos.RankingPageRequest;
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

    @GetMapping("/investor-rankings")
    public ResponseEntity<RankingPageDTO<TopInvestorDTO>> getInvestorRankings(
            @Valid @ModelAttribute RankingPageRequest request) {
        return ResponseEntity.ok(topAnalyticsService.getInvestorRankings(request.getPage(), request.getSize()));
    }

    @GetMapping("/instrument-rankings")
    public ResponseEntity<RankingPageDTO<TopInstrumentDTO>> getInstrumentRankings(
            @Valid @ModelAttribute RankingPageRequest request) {
        return ResponseEntity.ok(topAnalyticsService.getInstrumentRankings(request.getPage(), request.getSize()));
    }
}
