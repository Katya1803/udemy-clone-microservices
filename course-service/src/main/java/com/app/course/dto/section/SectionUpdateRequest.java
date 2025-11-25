package com.app.course.dto.section;

import jakarta.validation.constraints.Size;

public record SectionUpdateRequest(
        @Size(max = 150, message = "Title must not exceed 150 characters")
        String title,

        Integer sortOrder
) {}