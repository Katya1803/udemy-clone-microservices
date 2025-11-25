package com.app.course.dto.enrollment;

import com.app.course.dto.progress.LectureProgressItem;

import java.util.List;

public record CourseProgressDetail(
        String enrollmentId,
        String courseId,
        String courseTitle,
        String enrolledAt,
        String completedAt,
        Integer totalLectures,
        Integer completedLectures,
        Integer progressPercentage,
        List<SectionProgressItem> sections
) {
    public record SectionProgressItem(
            String sectionId,
            String sectionTitle,
            Integer sortOrder,
            List<LectureProgressItem> lectures
    ) {}
}