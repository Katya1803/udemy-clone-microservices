package com.app.course.dto.post;

public record SeriesInfoDto(
        String id,
        String title,
        String slug,
        Integer orderInSeries
) {}