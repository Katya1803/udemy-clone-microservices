package com.app.course.dto.lecture;

import jakarta.validation.constraints.Size;

public record LectureUpdateRequest(
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        String content,

        String videoUrl,

        Integer duration,

        Integer sortOrder,

        Boolean isPreview
) {}