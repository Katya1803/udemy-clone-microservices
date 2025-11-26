package com.app.course.service.impl;

import com.app.common.exception.ResourceNotFoundException;
import com.app.course.dto.enrollment.CourseProgressDetail;
import com.app.course.dto.enrollment.EnrollmentDetail;
import com.app.course.dto.enrollment.EnrollmentRequest;
import com.app.course.dto.enrollment.EnrollmentResponse;
import com.app.course.dto.progress.LectureProgressItem;
import com.app.course.entity.*;
import com.app.course.repository.*;
import com.app.course.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final LectureRepository lectureRepository;
    private final LectureProgressRepository lectureProgressRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public EnrollmentResponse enrollInCourse(EnrollmentRequest request, String accountId) {
        log.info("User {} enrolling in course: {}", accountId, request.courseId());

        // Validate course exists and is published
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!Course.CourseStatus.PUBLISHED.equals(course.getStatus())) {
            throw new IllegalStateException("Cannot enroll in unpublished course");
        }

        // Check if already enrolled
        if (enrollmentRepository.existsByAccountIdAndCourseId(accountId, request.courseId())) {
            throw new IllegalStateException("Already enrolled in this course");
        }

        Enrollment enrollment = Enrollment.builder()
                .accountId(accountId)
                .course(course)
                .enrolledAt(Instant.now())
                .progressPercentage(0)
                .build();

        enrollment = enrollmentRepository.save(enrollment);

        // Initialize lecture progress for all lectures
        initializeLectureProgress(enrollment);

        return mapToEnrollmentResponse(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentDetail> getMyEnrollments(String accountId, Pageable pageable) {
        log.debug("Fetching enrollments for user: {}", accountId);

        Page<Enrollment> enrollments = enrollmentRepository.findByAccountId(accountId, pageable);

        return enrollments.map(this::mapToEnrollmentDetail);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseProgressDetail getCourseProgress(String courseId, String accountId) {
        log.debug("Fetching progress for user: {} in course: {}", accountId, courseId);

        Enrollment enrollment = enrollmentRepository.findByAccountIdAndCourseId(accountId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        Course course = enrollment.getCourse();
        List<Section> sections = sectionRepository.findByCourseIdOrderBySortOrderAsc(courseId);

        // Get all lecture progresses for this enrollment
        List<LectureProgress> allProgresses = lectureProgressRepository.findByEnrollmentId(enrollment.getId());
        Map<String, LectureProgress> progressMap = allProgresses.stream()
                .collect(Collectors.toMap(lp -> lp.getLecture().getId(), lp -> lp));

        int totalLectures = 0;
        int completedLectures = 0;

        List<CourseProgressDetail.SectionProgressItem> sectionProgressItems = new ArrayList<>();

        for (Section section : sections) {
            List<Lecture> lectures = lectureRepository.findBySectionIdOrderBySortOrderAsc(section.getId());
            List<LectureProgressItem> lectureProgressItems = new ArrayList<>();

            for (Lecture lecture : lectures) {
                totalLectures++;
                LectureProgress progress = progressMap.get(lecture.getId());

                boolean isCompleted = progress != null && progress.getIsCompleted();
                if (isCompleted) {
                    completedLectures++;
                }

                lectureProgressItems.add(new LectureProgressItem(
                        lecture.getId(),
                        lecture.getTitle(),
                        lecture.getType().name(),
                        lecture.getDuration(),
                        lecture.getSortOrder(),
                        isCompleted,
                        progress != null ? formatInstant(progress.getCompletedAt()) : null,
                        progress != null ? progress.getLastWatchedPosition() : 0
                ));
            }

            sectionProgressItems.add(new CourseProgressDetail.SectionProgressItem(
                    section.getId(),
                    section.getTitle(),
                    section.getSortOrder(),
                    lectureProgressItems
            ));
        }

        return new CourseProgressDetail(
                enrollment.getId(),
                course.getId(),
                course.getTitle(),
                formatInstant(enrollment.getEnrolledAt()),
                formatInstant(enrollment.getCompletedAt()),
                totalLectures,
                completedLectures,
                enrollment.getProgressPercentage(),
                sectionProgressItems
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEnrolled(String courseId, String accountId) {
        return enrollmentRepository.existsByAccountIdAndCourseId(accountId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollment(String courseId, String accountId) {
        Enrollment enrollment = enrollmentRepository.findByAccountIdAndCourseId(accountId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        return mapToEnrollmentResponse(enrollment);
    }

    // ==================== Helper Methods ====================

    private void initializeLectureProgress(Enrollment enrollment) {
        List<Lecture> lectures = lectureRepository.findLecturesByCourseId(enrollment.getCourse().getId());

        for (Lecture lecture : lectures) {
            LectureProgress progress = LectureProgress.builder()
                    .enrollment(enrollment)
                    .lecture(lecture)
                    .isCompleted(false)
                    .lastWatchedPosition(0)
                    .build();

            lectureProgressRepository.save(progress);
        }
    }

    // ==================== Mapper Methods ====================

    private EnrollmentResponse mapToEnrollmentResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getAccountId(),
                enrollment.getCourse().getId(),
                formatInstant(enrollment.getEnrolledAt()),
                formatInstant(enrollment.getCompletedAt()),
                enrollment.getProgressPercentage()
        );
    }

    private EnrollmentDetail mapToEnrollmentDetail(Enrollment enrollment) {
        Course course = enrollment.getCourse();
        int totalLectures = (int) lectureRepository.countByCourseId(course.getId());

        EnrollmentDetail.CourseInfo courseInfo = new EnrollmentDetail.CourseInfo(
                course.getId(),
                course.getTitle(),
                course.getSlug(),
                course.getThumbnail(),
                totalLectures
        );

        return new EnrollmentDetail(
                enrollment.getId(),
                formatInstant(enrollment.getEnrolledAt()),
                formatInstant(enrollment.getCompletedAt()),
                enrollment.getProgressPercentage(),
                courseInfo
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