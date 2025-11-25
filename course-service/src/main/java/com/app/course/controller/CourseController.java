package com.app.course.controller;

import com.app.common.dto.response.ApiResponse;
import com.app.common.dto.response.PageResponse;
import com.app.common.util.CurrentAccount;
import com.app.course.dto.course.*;
import com.app.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * Get all published courses (public access)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CourseListItem>>> getPublishedCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {

        log.debug("Fetching published courses, page: {}, size: {}, keyword: {}", page, size, keyword);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CourseListItem> courses = courseService.getPublishedCourses(pageable, keyword);
        PageResponse<CourseListItem> response = PageResponse.of(courses);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get course by slug (public access)
     */
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<CourseDetail>> getCourseBySlug(@PathVariable String slug) {
        log.debug("Fetching course by slug: {}", slug);

        CourseDetail response = courseService.getCourseBySlug(slug);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all courses for admin (including drafts)
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<CourseListItem>>> getAllCoursesForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        log.debug("Admin fetching all courses, page: {}, size: {}, status: {}", page, size, status);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CourseListItem> courses = courseService.getAllCourses(pageable, status);
        PageResponse<CourseListItem> response = PageResponse.of(courses);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get course by ID (admin access)
     */
    @GetMapping("/admin/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseDetail>> getCourseById(@PathVariable String courseId) {
        log.debug("Admin fetching course by id: {}", courseId);

        CourseDetail response = courseService.getCourseById(courseId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Create course (admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @Valid @RequestBody CourseCreateRequest request,
            @CurrentAccount String adminId) {

        log.info("Creating course by admin: {}", adminId);

        CourseResponse response = courseService.createCourse(request, adminId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Course created successfully"));
    }

    /**
     * Update course (admin only)
     */
    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable String courseId,
            @Valid @RequestBody CourseUpdateRequest request,
            @CurrentAccount String adminId) {

        log.info("Updating course: {} by admin: {}", courseId, adminId);

        CourseResponse response = courseService.updateCourse(courseId, request, adminId);

        return ResponseEntity.ok(ApiResponse.success(response, "Course updated successfully"));
    }

    /**
     * Update course status (admin only)
     */
    @PatchMapping("/{courseId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourseStatus(
            @PathVariable String courseId,
            @Valid @RequestBody UpdateCourseStatusRequest request,
            @CurrentAccount String adminId) {

        log.info("Updating course status: {} to {} by admin: {}", courseId, request.status(), adminId);

        CourseResponse response = courseService.updateCourseStatus(courseId, request, adminId);

        return ResponseEntity.ok(ApiResponse.success(response, "Course status updated successfully"));
    }

    /**
     * Delete course (admin only)
     */
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @PathVariable String courseId,
            @CurrentAccount String adminId) {

        log.info("Deleting course: {} by admin: {}", courseId, adminId);

        courseService.deleteCourse(courseId, adminId);

        return ResponseEntity.ok(ApiResponse.success("Course deleted successfully"));
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Course service is running"));
    }
}