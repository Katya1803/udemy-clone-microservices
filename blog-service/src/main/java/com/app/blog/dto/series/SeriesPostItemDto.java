package com.app.blog.dto.series;

// post of a series with order
public record SeriesPostItemDto(
        String id,
        String title,
        String slug,
        Integer order
) {}
