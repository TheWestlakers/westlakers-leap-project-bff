package com.westlakers.leap_bff.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NewsResponseDTO(
        int totalResults,
        List<NewsArticleDTO> articles) {
}
