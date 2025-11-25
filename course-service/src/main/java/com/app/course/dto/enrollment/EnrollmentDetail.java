package com.app.course.dto.enrollment;

public record EnrollmentDetail(
        String id,
        String enrolledAt,
        String completedAt,
        Integer progressPercentage,
        CourseInfo course
) {
    public record CourseInfo(
            String id,
            String title,
            String slug,
            String thumbnail,
            Integer totalLectures
    ) {}
}