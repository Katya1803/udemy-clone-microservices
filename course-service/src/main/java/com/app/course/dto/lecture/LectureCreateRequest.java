package com.app.course.dto.lecture;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LectureCreateRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @NotBlank(message = "Type is required")
        @Pattern(regexp = "VIDEO|ARTICLE", message = "Type must be either VIDEO or ARTICLE")
        String type,

        String content,

        String videoUrl,

        Integer duration,

        @NotNull(message = "Sort order is required")
        Integer sortOrder,

        Boolean isPreview
) {}