package com.app.course.controller;

import com.app.common.dto.response.ApiResponse;
import com.app.common.util.CurrentAccount;
import com.app.course.dto.section.SectionCreateRequest;
import com.app.course.dto.section.SectionResponse;
import com.app.course.dto.section.SectionUpdateRequest;
import com.app.course.service.SectionService;
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
public class SectionController {

    private final SectionService sectionService;

    /**
     * Create section (admin only)
     */
    @PostMapping("/{courseId}/sections")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SectionResponse>> createSection(
            @PathVariable String courseId,
            @Valid @RequestBody SectionCreateRequest request,
            @CurrentAccount String adminId) {

        log.info("Creating section for course: {} by admin: {}", courseId, adminId);

        SectionResponse response = sectionService.createSection(courseId, request, adminId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Section created successfully"));
    }

    /**
     * Update section (admin only)
     */
    @PutMapping("/sections/{sectionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SectionResponse>> updateSection(
            @PathVariable String sectionId,
            @Valid @RequestBody SectionUpdateRequest request,
            @CurrentAccount String adminId) {

        log.info("Updating section: {} by admin: {}", sectionId, adminId);

        SectionResponse response = sectionService.updateSection(sectionId, request, adminId);

        return ResponseEntity.ok(ApiResponse.success(response, "Section updated successfully"));
    }

    /**
     * Delete section (admin only)
     */
    @DeleteMapping("/sections/{sectionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSection(
            @PathVariable String sectionId,
            @CurrentAccount String adminId) {

        log.info("Deleting section: {} by admin: {}", sectionId, adminId);

        sectionService.deleteSection(sectionId, adminId);

        return ResponseEntity.ok(ApiResponse.success("Section deleted successfully"));
    }

    /**
     * Get sections by course ID
     */
    @GetMapping("/{courseId}/sections")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> getSectionsByCourseId(
            @PathVariable String courseId) {

        log.debug("Fetching sections for course: {}", courseId);

        List<SectionResponse> response = sectionService.getSectionsByCourseId(courseId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}