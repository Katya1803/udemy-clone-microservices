package com.app.course.service;

import com.app.course.dto.course.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseService {

    /**
     * Get all published courses (public access)
     */
    Page<CourseListItem> getPublishedCourses(Pageable pageable, String keyword);

    /**
     * Get course by slug (public access)
     */
    CourseDetail getCourseBySlug(String slug);

    /**
     * Get course by ID (admin access)
     */
    CourseDetail getCourseById(String courseId);

    /**
     * Create new course (admin only)
     */
    CourseResponse createCourse(CourseCreateRequest request, String adminId);

    /**
     * Update course (admin only)
     */
    CourseResponse updateCourse(String courseId, CourseUpdateRequest request, String adminId);

    /**
     * Update course status (admin only)
     */
    CourseResponse updateCourseStatus(String courseId, UpdateCourseStatusRequest request, String adminId);

    /**
     * Delete course (admin only)
     */
    void deleteCourse(String courseId, String adminId);

    /**
     * Get all courses for admin (including drafts)
     */
    Page<CourseListItem> getAllCourses(Pageable pageable, String status);
}