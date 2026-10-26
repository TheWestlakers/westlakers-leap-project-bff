package com.westlakers.leap_bff.controllers;

import com.westlakers.leap_bff.dtos.NewsResponseDTO;
import com.westlakers.leap_bff.services.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
public class NewsController {
    private final NewsService newsService;

    @GetMapping("/market")
    public NewsResponseDTO getMarketHeadlines() {
        return newsService.getMarketHeadlines();
    }

    @GetMapping("/ticker/{symbol}")
    public NewsResponseDTO getTickerHeadlines(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "") String companyName,
            @RequestParam(defaultValue = "") String sector) {
        return newsService.getTickerHeadlines(symbol, companyName, sector);
    }
}
