package com.app.course.controller;

import com.app.common.dto.response.ApiResponse;
import com.app.common.dto.response.PageResponse;
import com.app.common.util.CurrentAccount;
import com.app.course.dto.enrollment.CourseProgressDetail;
import com.app.course.dto.enrollment.EnrollmentDetail;
import com.app.course.dto.enrollment.EnrollmentRequest;
import com.app.course.dto.enrollment.EnrollmentResponse;
import com.app.course.service.EnrollmentService;
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
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Enroll in course (authenticated users)
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollInCourse(
            @Valid @RequestBody EnrollmentRequest request,
            @CurrentAccount String accountId) {

        log.info("User {} enrolling in course: {}", accountId, request.courseId());

        EnrollmentResponse response = enrollmentService.enrollInCourse(request, accountId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Successfully enrolled in course"));
    }

    /**
     * Get my enrollments (authenticated users)
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<EnrollmentDetail>>> getMyEnrollments(
            @CurrentAccount String accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("Fetching enrollments for user: {}", accountId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("enrolledAt").descending());
        Page<EnrollmentDetail> enrollments = enrollmentService.getMyEnrollments(accountId, pageable);
        PageResponse<EnrollmentDetail> response = PageResponse.of(enrollments);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get course progress (authenticated users)
     */
    @GetMapping("/{courseId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CourseProgressDetail>> getCourseProgress(
            @PathVariable String courseId,
            @CurrentAccount String accountId) {

        log.debug("Fetching progress for user: {} in course: {}", accountId, courseId);

        CourseProgressDetail response = enrollmentService.getCourseProgress(courseId, accountId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Check if enrolled (authenticated users)
     */
    @GetMapping("/{courseId}/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> checkEnrollment(
            @PathVariable String courseId,
            @CurrentAccount String accountId) {

        log.debug("Checking enrollment for user: {} in course: {}", accountId, courseId);

        boolean isEnrolled = enrollmentService.isEnrolled(courseId, accountId);

        return ResponseEntity.ok(ApiResponse.success(isEnrolled));
    }
}