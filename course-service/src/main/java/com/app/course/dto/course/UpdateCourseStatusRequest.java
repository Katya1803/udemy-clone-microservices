package com.app.course.dto.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateCourseStatusRequest(
        @NotBlank(message = "Status is required")
        @Pattern(regexp = "DRAFT|PUBLISHED", message = "Status must be either DRAFT or PUBLISHED")
        String status
) {}