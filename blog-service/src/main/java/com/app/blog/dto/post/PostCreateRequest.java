package com.app.blog.dto.post;

public record PostCreateRequest(
        String title,
        String slug,
        String excerpt,
        String content,
        String seriesId,
        Integer orderInSeries
) {}
