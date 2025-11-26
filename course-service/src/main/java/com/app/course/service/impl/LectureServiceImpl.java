package com.app.course.service.impl;

import com.app.common.exception.ResourceNotFoundException;
import com.app.course.dto.lecture.LectureCreateRequest;
import com.app.course.dto.lecture.LectureResponse;
import com.app.course.dto.lecture.LectureUpdateRequest;
import com.app.course.entity.Lecture;
import com.app.course.entity.Section;
import com.app.course.repository.LectureRepository;
import com.app.course.repository.SectionRepository;
import com.app.course.service.LectureService;
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
public class LectureServiceImpl implements LectureService {

    private final LectureRepository lectureRepository;
    private final SectionRepository sectionRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public LectureResponse createLecture(String sectionId, LectureCreateRequest request, String adminId) {
        log.info("Creating lecture for section: {} by admin: {}", sectionId, adminId);

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        // Validate lecture type and content
        validateLectureContent(request.type(), request.content(), request.videoUrl());

        Lecture.LectureType type = Lecture.LectureType.valueOf(request.type());

        Lecture lecture = Lecture.builder()
                .section(section)
                .title(request.title())
                .type(type)
                .content(request.content())
                .videoUrl(request.videoUrl())
                .duration(request.duration())
                .sortOrder(request.sortOrder())
                .isPreview(request.isPreview() != null ? request.isPreview() : false)
                .build();

        lecture = lectureRepository.save(lecture);

        return mapToLectureResponse(lecture);
    }

    @Override
    @Transactional
    public LectureResponse updateLecture(String lectureId, LectureUpdateRequest request, String adminId) {
        log.info("Updating lecture: {} by admin: {}", lectureId, adminId);

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found"));

        if (StringUtils.hasText(request.title())) {
            lecture.setTitle(request.title());
        }
        if (request.content() != null) {
            lecture.setContent(request.content());
        }
        if (request.videoUrl() != null) {
            lecture.setVideoUrl(request.videoUrl());
        }
        if (request.duration() != null) {
            lecture.setDuration(request.duration());
        }
        if (request.sortOrder() != null) {
            lecture.setSortOrder(request.sortOrder());
        }
        if (request.isPreview() != null) {
            lecture.setIsPreview(request.isPreview());
        }

        lecture = lectureRepository.save(lecture);

        return mapToLectureResponse(lecture);
    }

    @Override
    @Transactional
    public void deleteLecture(String lectureId, String adminId) {
        log.info("Deleting lecture: {} by admin: {}", lectureId, adminId);

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found"));

        lectureRepository.delete(lecture);
    }

    @Override
    @Transactional(readOnly = true)
    public LectureResponse getLectureById(String lectureId) {
        log.debug("Fetching lecture: {}", lectureId);

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found"));

        return mapToLectureResponse(lecture);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LectureResponse> getLecturesBySectionId(String sectionId) {
        log.debug("Fetching lectures for section: {}", sectionId);

        List<Lecture> lectures = lectureRepository.findBySectionIdOrderBySortOrderAsc(sectionId);

        return lectures.stream()
                .map(this::mapToLectureResponse)
                .collect(Collectors.toList());
    }

    // ==================== Validation ====================

    private void validateLectureContent(String type, String content, String videoUrl) {
        Lecture.LectureType lectureType = Lecture.LectureType.valueOf(type);

        if (lectureType == Lecture.LectureType.VIDEO) {
            if (!StringUtils.hasText(videoUrl)) {
                throw new IllegalArgumentException("Video URL is required for VIDEO type lectures");
            }
        } else if (lectureType == Lecture.LectureType.ARTICLE) {
            if (!StringUtils.hasText(content)) {
                throw new IllegalArgumentException("Content is required for ARTICLE type lectures");
            }
        }
    }

    // ==================== Mapper Methods ====================

    private LectureResponse mapToLectureResponse(Lecture lecture) {
        return new LectureResponse(
                lecture.getId(),
                lecture.getSection().getId(),
                lecture.getTitle(),
                lecture.getType().name(),
                lecture.getContent(),
                lecture.getVideoUrl(),
                lecture.getDuration(),
                lecture.getSortOrder(),
                lecture.getIsPreview(),
                formatInstant(lecture.getCreatedAt()),
                formatInstant(lecture.getUpdatedAt())
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