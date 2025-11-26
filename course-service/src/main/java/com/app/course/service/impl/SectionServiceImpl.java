package com.app.course.service.impl;

import com.app.common.exception.ResourceNotFoundException;
import com.app.course.dto.section.SectionCreateRequest;
import com.app.course.dto.section.SectionResponse;
import com.app.course.dto.section.SectionUpdateRequest;
import com.app.course.entity.Course;
import com.app.course.entity.Section;
import com.app.course.repository.CourseRepository;
import com.app.course.repository.LectureRepository;
import com.app.course.repository.SectionRepository;
import com.app.course.service.SectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final LectureRepository lectureRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public SectionResponse createSection(String courseId, SectionCreateRequest request, String adminId) {
        log.info("Creating section for course: {} by admin: {}", courseId, adminId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Section section = Section.builder()
                .course(course)
                .title(request.title())
                .sortOrder(request.sortOrder())
                .build();

        section = sectionRepository.save(section);

        return mapToSectionResponse(section);
    }

    @Override
    @Transactional
    public SectionResponse updateSection(String sectionId, SectionUpdateRequest request, String adminId) {
        log.info("Updating section: {} by admin: {}", sectionId, adminId);

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        if (StringUtils.hasText(request.title())) {
            section.setTitle(request.title());
        }
        if (request.sortOrder() != null) {
            section.setSortOrder(request.sortOrder());
        }

        section = sectionRepository.save(section);

        return mapToSectionResponse(section);
    }

    @Override
    @Transactional
    public void deleteSection(String sectionId, String adminId) {
        log.info("Deleting section: {} by admin: {}", sectionId, adminId);

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        // Cascade delete will handle lectures
        sectionRepository.delete(section);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionResponse> getSectionsByCourseId(String courseId) {
        log.debug("Fetching sections for course: {}", courseId);

        List<Section> sections = sectionRepository.findByCourseIdOrderBySortOrderAsc(courseId);

        return sections.stream()
                .map(this::mapToSectionResponse)
                .collect(Collectors.toList());
    }

    // ==================== Mapper Methods ====================

    private SectionResponse mapToSectionResponse(Section section) {
        int totalLectures = (int) lectureRepository.countBySectionId(section.getId());

        return new SectionResponse(
                section.getId(),
                section.getCourse().getId(),
                section.getTitle(),
                section.getSortOrder(),
                totalLectures,
                formatInstant(section.getCreatedAt()),
                formatInstant(section.getUpdatedAt())
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