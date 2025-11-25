package com.app.course.dto.progress;

public record LectureProgressResponse(
        String id,
        String enrollmentId,
        String lectureId,
        Boolean isCompleted,
        String completedAt,
        Integer lastWatchedPosition
) {}