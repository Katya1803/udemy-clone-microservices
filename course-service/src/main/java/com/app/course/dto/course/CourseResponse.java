package com.app.course.dto.course;

public record CourseResponse(
        String id,
        String title,
        String slug,
        String description,
        String thumbnail,
        String status,
        String createdAt,
        String updatedAt
) {}