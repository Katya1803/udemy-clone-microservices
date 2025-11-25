package com.app.course.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostCreateRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @NotBlank(message = "Slug is required")
        @Size(max = 200, message = "Slug must not exceed 200 characters")
        String slug,

        String excerpt,

        @NotBlank(message = "Content is required")
        String content,

        String seriesId,
        Integer orderInSeries
) {}