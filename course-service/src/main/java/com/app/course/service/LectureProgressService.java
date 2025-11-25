package com.app.course.service;

import com.app.course.dto.progress.LectureProgressRequest;
import com.app.course.dto.progress.LectureProgressResponse;

public interface LectureProgressService {

    /**
     * Update lecture progress
     */
    LectureProgressResponse updateProgress(String lectureId, LectureProgressRequest request, String accountId);

    /**
     * Mark lecture as complete
     */
    LectureProgressResponse markAsComplete(String lectureId, String accountId);

    /**
     * Get lecture progress
     */
    LectureProgressResponse getLectureProgress(String lectureId, String accountId);
}