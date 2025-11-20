package com.app.blog.dto.series;

public record SeriesCreateRequest(
        String title,
        String slug,
        String description,
        String thumbnail
) {}
