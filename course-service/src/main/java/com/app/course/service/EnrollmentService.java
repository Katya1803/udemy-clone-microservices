package com.app.course.service;

import com.app.course.dto.enrollment.CourseProgressDetail;
import com.app.course.dto.enrollment.EnrollmentDetail;
import com.app.course.dto.enrollment.EnrollmentRequest;
import com.app.course.dto.enrollment.EnrollmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EnrollmentService {

    /**
     * Enroll user in a course
     */
    EnrollmentResponse enrollInCourse(EnrollmentRequest request, String accountId);

    /**
     * Get user's enrollments
     */
    Page<EnrollmentDetail> getMyEnrollments(String accountId, Pageable pageable);

    /**
     * Get course progress for user
     */
    CourseProgressDetail getCourseProgress(String courseId, String accountId);

    /**
     * Check if user is enrolled in course
     */
    boolean isEnrolled(String courseId, String accountId);

    /**
     * Get enrollment by course ID and account ID
     */
    EnrollmentResponse getEnrollment(String courseId, String accountId);
}