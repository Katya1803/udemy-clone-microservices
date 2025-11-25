package com.app.course.dto.series;

import java.util.List;

// return details of a specific series
public record SeriesDetailDto(
        String id,
        String title,
        String slug,
        String description,
        String thumbnail,
        List<SeriesPostItemDto> posts
) {}