package com.app.blog.dto.series;

// return list of all series when FE call GET/series
public record SeriesListItemDto(
        String id,
        String title,
        String slug
) {}
