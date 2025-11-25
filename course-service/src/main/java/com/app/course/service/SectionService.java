package com.app.course.service;

import com.app.course.dto.section.SectionCreateRequest;
import com.app.course.dto.section.SectionResponse;
import com.app.course.dto.section.SectionUpdateRequest;

import java.util.List;

public interface SectionService {

    /**
     * Create new section for a course (admin only)
     */
    SectionResponse createSection(String courseId, SectionCreateRequest request, String adminId);

    /**
     * Update section (admin only)
     */
    SectionResponse updateSection(String sectionId, SectionUpdateRequest request, String adminId);

    /**
     * Delete section (admin only)
     */
    void deleteSection(String sectionId, String adminId);

    /**
     * Get sections by course ID
     */
    List<SectionResponse> getSectionsByCourseId(String courseId);
}