package com.app.course.dto.lecture;

public record LectureListItem(
        String id,
        String title,
        String type,
        Integer duration,
        Integer sortOrder,
        Boolean isPreview
) {}