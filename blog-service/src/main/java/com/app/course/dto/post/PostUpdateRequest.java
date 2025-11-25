package com.app.course.dto.post;

import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @Size(max = 200, message = "Slug must not exceed 200 characters")
        String slug,

        String content,
        String status,
        String seriesId,
        Integer orderInSeries
) {}