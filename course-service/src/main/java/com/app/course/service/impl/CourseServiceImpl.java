package com.app.course.service.impl;

import com.app.common.exception.ResourceNotFoundException;
import com.app.course.dto.course.*;
import com.app.course.dto.lecture.LectureListItem;
import com.app.course.dto.section.SectionWithLectures;
import com.app.course.entity.Course;
import com.app.course.entity.Lecture;
import com.app.course.entity.Section;
import com.app.course.repository.CourseRepository;
import com.app.course.repository.LectureRepository;
import com.app.course.repository.SectionRepository;
import com.app.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final LectureRepository lectureRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(readOnly = true)
    public Page<CourseListItem> getPublishedCourses(Pageable pageable, String keyword) {
        log.debug("Fetching published courses, keyword: {}", keyword);

        Page<Course> courses;
        if (StringUtils.hasText(keyword)) {
            courses = courseRepository.searchByStatusAndKeyword(
                    Course.CourseStatus.PUBLISHED, keyword, pageable);
        } else {
            courses = courseRepository.findByStatus(Course.CourseStatus.PUBLISHED, pageable);
        }

        return courses.map(this::mapToCourseListItem);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetail getCourseBySlug(String slug) {
        log.debug("Fetching course by slug: {}", slug);

        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!Course.CourseStatus.PUBLISHED.equals(course.getStatus())) {
            throw new ResourceNotFoundException("Course not found");
        }

        return mapToCourseDetail(course);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetail getCourseById(String courseId) {
        log.debug("Fetching course by id: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        return mapToCourseDetail(course);
    }

    @Override
    @Transactional
    public CourseResponse createCourse(CourseCreateRequest request, String adminId) {
        log.info("Creating course: {} by admin: {}", request.title(), adminId);

        // Validate slug uniqueness
        if (courseRepository.existsBySlug(request.slug())) {
            throw new IllegalArgumentException("Course with this slug already exists");
        }

        Course course = Course.builder()
                .title(request.title())
                .slug(request.slug())
                .description(request.description())
                .thumbnail(request.thumbnail())
                .status(Course.CourseStatus.DRAFT)
                .build();

        course = courseRepository.save(course);

        return mapToCourseResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(String courseId, CourseUpdateRequest request, String adminId) {
        log.info("Updating course: {} by admin: {}", courseId, adminId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Update fields if provided
        if (StringUtils.hasText(request.title())) {
            course.setTitle(request.title());
        }
        if (StringUtils.hasText(request.slug())) {
            // Validate slug uniqueness (excluding current course)
            courseRepository.findBySlug(request.slug()).ifPresent(existing -> {
                if (!existing.getId().equals(courseId)) {
                    throw new IllegalArgumentException("Course with this slug already exists");
                }
            });
            course.setSlug(request.slug());
        }
        if (request.description() != null) {
            course.setDescription(request.description());
        }
        if (request.thumbnail() != null) {
            course.setThumbnail(request.thumbnail());
        }

        course = courseRepository.save(course);

        return mapToCourseResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse updateCourseStatus(String courseId, UpdateCourseStatusRequest request, String adminId) {
        log.info("Updating course status: {} to {} by admin: {}", courseId, request.status(), adminId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Course.CourseStatus newStatus = Course.CourseStatus.valueOf(request.status());
        course.setStatus(newStatus);

        course = courseRepository.save(course);

        return mapToCourseResponse(course);
    }

    @Override
    @Transactional
    public void deleteCourse(String courseId, String adminId) {
        log.info("Deleting course: {} by admin: {}", courseId, adminId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Cascade delete will handle sections and lectures
        courseRepository.delete(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseListItem> getAllCourses(Pageable pageable, String status) {
        log.debug("Admin fetching all courses, status filter: {}", status);

        Page<Course> courses;
        if (StringUtils.hasText(status)) {
            Course.CourseStatus courseStatus = Course.CourseStatus.valueOf(status.toUpperCase());
            courses = courseRepository.findByStatus(courseStatus, pageable);
        } else {
            courses = courseRepository.findAll(pageable);
        }

        return courses.map(this::mapToCourseListItem);
    }

    // ==================== Mapper Methods ====================

    private CourseListItem mapToCourseListItem(Course course) {
        int totalSections = sectionRepository.findByCourseIdOrderBySortOrderAsc(course.getId()).size();
        int totalLectures = (int) lectureRepository.countByCourseId(course.getId());

        return new CourseListItem(
                course.getId(),
                course.getTitle(),
                course.getSlug(),
                course.getDescription(),
                course.getThumbnail(),
                course.getStatus().name(),
                totalSections,
                totalLectures,
                formatInstant(course.getCreatedAt())
        );
    }

    private CourseDetail mapToCourseDetail(Course course) {
        List<Section> sections = sectionRepository.findByCourseIdOrderBySortOrderAsc(course.getId());
        int totalLectures = (int) lectureRepository.countByCourseId(course.getId());

        List<SectionWithLectures> sectionDtos = sections.stream()
                .map(this::mapToSectionWithLectures)
                .collect(Collectors.toList());

        return new CourseDetail(
                course.getId(),
                course.getTitle(),
                course.getSlug(),
                course.getDescription(),
                course.getThumbnail(),
                course.getStatus().name(),
                sections.size(),
                totalLectures,
                formatInstant(course.getCreatedAt()),
                formatInstant(course.getUpdatedAt()),
                sectionDtos
        );
    }

    private SectionWithLectures mapToSectionWithLectures(Section section) {
        List<Lecture> lectures = lectureRepository.findBySectionIdOrderBySortOrderAsc(section.getId());

        List<LectureListItem> lectureDtos = lectures.stream()
                .map(lecture -> new LectureListItem(
                        lecture.getId(),
                        lecture.getTitle(),
                        lecture.getType().name(),
                        lecture.getDuration(),
                        lecture.getSortOrder(),
                        lecture.getIsPreview()
                ))
                .collect(Collectors.toList());

        return new SectionWithLectures(
                section.getId(),
                section.getTitle(),
                section.getSortOrder(),
                lectureDtos
        );
    }

    private CourseResponse mapToCourseResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getSlug(),
                course.getDescription(),
                course.getThumbnail(),
                course.getStatus().name(),
                formatInstant(course.getCreatedAt()),
                formatInstant(course.getUpdatedAt())
        );
    }

    private String formatInstant(Instant instant) {
        if (instant == null) {
            return "";
        }
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return dateTime.format(FORMATTER);
    }
}