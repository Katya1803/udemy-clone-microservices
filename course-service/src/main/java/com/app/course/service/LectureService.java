package com.app.course.service;

import com.app.course.dto.lecture.LectureCreateRequest;
import com.app.course.dto.lecture.LectureResponse;
import com.app.course.dto.lecture.LectureUpdateRequest;

import java.util.List;

public interface LectureService {

    /**
     * Create new lecture for a section (admin only)
     */
    LectureResponse createLecture(String sectionId, LectureCreateRequest request, String adminId);

    /**
     * Update lecture (admin only)
     */
    LectureResponse updateLecture(String lectureId, LectureUpdateRequest request, String adminId);

    /**
     * Delete lecture (admin only)
     */
    void deleteLecture(String lectureId, String adminId);

    /**
     * Get lecture by ID
     */
    LectureResponse getLectureById(String lectureId);

    /**
     * Get lectures by section ID
     */
    List<LectureResponse> getLecturesBySectionId(String sectionId);
}