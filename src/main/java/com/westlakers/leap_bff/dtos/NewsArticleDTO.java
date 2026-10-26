package com.westlakers.leap_bff.dtos;

public record NewsArticleDTO(
        String sourceName,
        String author,
        String title,
        String description,
        String url,
        String imageUrl,
        String publishedAt) {
}
