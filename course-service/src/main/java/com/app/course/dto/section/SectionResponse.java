package com.app.course.dto.section;

public record SectionResponse(
        String id,
        String courseId,
        String title,
        Integer sortOrder,
        Integer totalLectures,
        String createdAt,
        String updatedAt
) {}