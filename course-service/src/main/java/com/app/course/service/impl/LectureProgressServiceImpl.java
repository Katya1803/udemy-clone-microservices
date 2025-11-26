package com.app.course.service.impl;

import com.app.common.exception.ResourceNotFoundException;
import com.app.course.dto.progress.LectureProgressRequest;
import com.app.course.dto.progress.LectureProgressResponse;
import com.app.course.entity.Enrollment;
import com.app.course.entity.Lecture;
import com.app.course.entity.LectureProgress;
import com.app.course.repository.EnrollmentRepository;
import com.app.course.repository.LectureProgressRepository;
import com.app.course.repository.LectureRepository;
import com.app.course.service.LectureProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class LectureProgressServiceImpl implements LectureProgressService {

    private final LectureProgressRepository lectureProgressRepository;
    private final LectureRepository lectureRepository;
    private final EnrollmentRepository enrollmentRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public LectureProgressResponse updateProgress(String lectureId, LectureProgressRequest request, String accountId) {
        log.info("Updating progress for lecture: {} by user: {}", lectureId, accountId);

        // Get lecture and validate
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found"));

        // Get enrollment
        Enrollment enrollment = enrollmentRepository.findByAccountIdAndCourseId(
                        accountId, lecture.getSection().getCourse().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found. Please enroll in the course first."));

        // Get or create lecture progress
        LectureProgress progress = lectureProgressRepository
                .findByEnrollmentIdAndLectureId(enrollment.getId(), lectureId)
                .orElseGet(() -> createNewLectureProgress(enrollment, lecture));

        // Update fields
        if (request.isCompleted() != null) {
            progress.setIsCompleted(request.isCompleted());
            if (request.isCompleted()) {
                progress.setCompletedAt(Instant.now());
            } else {
                progress.setCompletedAt(null);
            }
        }

        if (request.lastWatchedPosition() != null) {
            progress.setLastWatchedPosition(request.lastWatchedPosition());
        }

        progress = lectureProgressRepository.save(progress);

        // Update enrollment progress percentage
        updateEnrollmentProgress(enrollment);

        return mapToLectureProgressResponse(progress);
    }

    @Override
    @Transactional
    public LectureProgressResponse markAsComplete(String lectureId, String accountId) {
        log.info("Marking lecture: {} as complete by user: {}", lectureId, accountId);

        // Get lecture and validate
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found"));

        // Get enrollment
        Enrollment enrollment = enrollmentRepository.findByAccountIdAndCourseId(
                        accountId, lecture.getSection().getCourse().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found. Please enroll in the course first."));

        // Get or create lecture progress
        LectureProgress progress = lectureProgressRepository
                .findByEnrollmentIdAndLectureId(enrollment.getId(), lectureId)
                .orElseGet(() -> createNewLectureProgress(enrollment, lecture));

        // Mark as complete
        progress.setIsCompleted(true);
        progress.setCompletedAt(Instant.now());

        progress = lectureProgressRepository.save(progress);

        // Update enrollment progress percentage
        updateEnrollmentProgress(enrollment);

        return mapToLectureProgressResponse(progress);
    }

    @Override
    @Transactional(readOnly = true)
    public LectureProgressResponse getLectureProgress(String lectureId, String accountId) {
        log.debug("Fetching progress for lecture: {} by user: {}", lectureId, accountId);

        LectureProgress progress = lectureProgressRepository
                .findByAccountIdAndLectureId(accountId, lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture progress not found"));

        return mapToLectureProgressResponse(progress);
    }

    // ==================== Helper Methods ====================

    private LectureProgress createNewLectureProgress(Enrollment enrollment, Lecture lecture) {
        return LectureProgress.builder()
                .enrollment(enrollment)
                .lecture(lecture)
                .isCompleted(false)
                .lastWatchedPosition(0)
                .build();
    }

    private void updateEnrollmentProgress(Enrollment enrollment) {
        long totalLectures = lectureProgressRepository.countByEnrollmentId(enrollment.getId());
        long completedLectures = lectureProgressRepository.countCompletedByEnrollmentId(enrollment.getId());

        if (totalLectures > 0) {
            int progressPercentage = (int) ((completedLectures * 100) / totalLectures);
            enrollment.setProgressPercentage(progressPercentage);

            // Mark course as completed if all lectures done
            if (progressPercentage == 100 && enrollment.getCompletedAt() == null) {
                enrollment.setCompletedAt(Instant.now());
                log.info("User {} completed course: {}", enrollment.getAccountId(), enrollment.getCourse().getId());
            }

            enrollmentRepository.save(enrollment);
        }
    }

    // ==================== Mapper Methods ====================

    private LectureProgressResponse mapToLectureProgressResponse(LectureProgress progress) {
        return new LectureProgressResponse(
                progress.getId(),
                progress.getEnrollment().getId(),
                progress.getLecture().getId(),
                progress.getIsCompleted(),
                formatInstant(progress.getCompletedAt()),
                progress.getLastWatchedPosition()
        );
    }

    private String formatInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return dateTime.format(FORMATTER);
    }
}