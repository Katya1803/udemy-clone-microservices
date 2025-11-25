package com.app.course.dto.course;

public record CourseListItem(
        String id,
        String title,
        String slug,
        String description,
        String thumbnail,
        String status,
        Integer totalSections,
        Integer totalLectures,
        String createdAt
) {}