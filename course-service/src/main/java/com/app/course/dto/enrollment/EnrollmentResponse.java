package com.app.course.dto.enrollment;

public record EnrollmentResponse(
        String id,
        String accountId,
        String courseId,
        String enrolledAt,
        String completedAt,
        Integer progressPercentage
) {}