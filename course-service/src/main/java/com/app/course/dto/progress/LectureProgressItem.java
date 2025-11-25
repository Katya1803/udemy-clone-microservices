package com.app.course.dto.progress;

public record LectureProgressItem(
        String lectureId,
        String lectureTitle,
        String lectureType,
        Integer duration,
        Integer sortOrder,
        Boolean isCompleted,
        String completedAt,
        Integer lastWatchedPosition
) {}