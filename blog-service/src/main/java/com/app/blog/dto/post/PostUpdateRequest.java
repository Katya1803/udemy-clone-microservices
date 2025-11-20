package com.app.blog.dto.post;

public record PostUpdateRequest(
        String title,
        String slug,
        String content,
        String status,
        String seriesId,
        Integer orderInSeries
) {}
