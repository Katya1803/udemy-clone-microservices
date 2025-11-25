package com.app.course.dto.lecture;

public record LectureResponse(
        String id,
        String sectionId,
        String title,
        String type,
        String content,
        String videoUrl,
        Integer duration,
        Integer sortOrder,
        Boolean isPreview,
        String createdAt,
        String updatedAt
) {}