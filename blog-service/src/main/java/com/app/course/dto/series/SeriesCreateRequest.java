package com.app.course.dto.series;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SeriesCreateRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must not exceed 150 characters")
        String title,

        @NotBlank(message = "Slug is required")
        @Size(max = 200, message = "Slug must not exceed 200 characters")
        String slug,

        String description,
        String thumbnail
) {}