package com.app.course.controller;

import com.app.common.dto.response.ApiResponse;
import com.app.common.util.CurrentAccount;
import com.app.course.dto.lecture.LectureCreateRequest;
import com.app.course.dto.lecture.LectureResponse;
import com.app.course.dto.lecture.LectureUpdateRequest;
import com.app.course.service.LectureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    /**
     * Create lecture (admin only)
     */
    @PostMapping("/sections/{sectionId}/lectures")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LectureResponse>> createLecture(
            @PathVariable String sectionId,
            @Valid @RequestBody LectureCreateRequest request,
            @CurrentAccount String adminId) {

        log.info("Creating lecture for section: {} by admin: {}", sectionId, adminId);

        LectureResponse response = lectureService.createLecture(sectionId, request, adminId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Lecture created successfully"));
    }

    /**
     * Update lecture (admin only)
     */
    @PutMapping("/lectures/{lectureId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LectureResponse>> updateLecture(
            @PathVariable String lectureId,
            @Valid @RequestBody LectureUpdateRequest request,
            @CurrentAccount String adminId) {

        log.info("Updating lecture: {} by admin: {}", lectureId, adminId);

        LectureResponse response = lectureService.updateLecture(lectureId, request, adminId);

        return ResponseEntity.ok(ApiResponse.success(response, "Lecture updated successfully"));
    }

    /**
     * Delete lecture (admin only)
     */
    @DeleteMapping("/lectures/{lectureId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLecture(
            @PathVariable String lectureId,
            @CurrentAccount String adminId) {

        log.info("Deleting lecture: {} by admin: {}", lectureId, adminId);

        lectureService.deleteLecture(lectureId, adminId);

        return ResponseEntity.ok(ApiResponse.success("Lecture deleted successfully"));
    }

    /**
     * Get lecture by ID
     */
    @GetMapping("/lectures/{lectureId}")
    public ResponseEntity<ApiResponse<LectureResponse>> getLectureById(@PathVariable String lectureId) {
        log.debug("Fetching lecture: {}", lectureId);

        LectureResponse response = lectureService.getLectureById(lectureId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get lectures by section ID
     */
    @GetMapping("/sections/{sectionId}/lectures")
    public ResponseEntity<ApiResponse<List<LectureResponse>>> getLecturesBySectionId(
            @PathVariable String sectionId) {

        log.debug("Fetching lectures for section: {}", sectionId);

        List<LectureResponse> response = lectureService.getLecturesBySectionId(sectionId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}