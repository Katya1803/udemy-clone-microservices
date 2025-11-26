package com.app.blog.dto.post;

public record SeriesInfoDto(
        String id,
        String title,
        String slug,
        Integer orderInSeries
) {}