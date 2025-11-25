package com.app.course.dto.progress;

public record LectureProgressRequest(
        Boolean isCompleted,
        Integer lastWatchedPosition
) {}