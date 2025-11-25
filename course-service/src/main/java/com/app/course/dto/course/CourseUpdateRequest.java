package com.app.course.dto.course;

import jakarta.validation.constraints.Size;

public record CourseUpdateRequest(
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @Size(max = 200, message = "Slug must not exceed 200 characters")
        String slug,

        String description,

        String thumbnail
) {}