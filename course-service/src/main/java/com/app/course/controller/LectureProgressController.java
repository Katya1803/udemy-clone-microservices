package com.app.course.controller;

import com.app.common.dto.response.ApiResponse;
import com.app.common.util.CurrentAccount;
import com.app.course.dto.progress.LectureProgressRequest;
import com.app.course.dto.progress.LectureProgressResponse;
import com.app.course.service.LectureProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/enrollments/lectures")
@RequiredArgsConstructor
public class LectureProgressController {

    private final LectureProgressService lectureProgressService;

    /**
     * Update lecture progress (authenticated users)
     */
    @PutMapping("/{lectureId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LectureProgressResponse>> updateProgress(
            @PathVariable String lectureId,
            @Valid @RequestBody LectureProgressRequest request,
            @CurrentAccount String accountId) {

        log.info("Updating progress for lecture: {} by user: {}", lectureId, accountId);

        LectureProgressResponse response = lectureProgressService.updateProgress(lectureId, request, accountId);

        return ResponseEntity.ok(ApiResponse.success(response, "Progress updated successfully"));
    }

    /**
     * Mark lecture as complete (authenticated users)
     */
    @PostMapping("/{lectureId}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LectureProgressResponse>> markAsComplete(
            @PathVariable String lectureId,
            @CurrentAccount String accountId) {

        log.info("Marking lecture: {} as complete by user: {}", lectureId, accountId);

        LectureProgressResponse response = lectureProgressService.markAsComplete(lectureId, accountId);

        return ResponseEntity.ok(ApiResponse.success(response, "Lecture marked as complete"));
    }

    /**
     * Get lecture progress (authenticated users)
     */
    @GetMapping("/{lectureId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LectureProgressResponse>> getLectureProgress(
            @PathVariable String lectureId,
            @CurrentAccount String accountId) {

        log.debug("Fetching progress for lecture: {} by user: {}", lectureId, accountId);

        LectureProgressResponse response = lectureProgressService.getLectureProgress(lectureId, accountId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}