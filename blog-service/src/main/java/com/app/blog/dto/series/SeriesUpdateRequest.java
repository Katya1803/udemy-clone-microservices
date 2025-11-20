package com.app.blog.dto.series;

public record SeriesUpdateRequest(
        String title,
        String slug,
        String description,
        String thumbnail
) {}
