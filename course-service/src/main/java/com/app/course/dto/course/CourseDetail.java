package com.app.course.dto.course;

import com.app.course.dto.section.SectionWithLectures;

import java.util.List;

public record CourseDetail(
        String id,
        String title,
        String slug,
        String description,
        String thumbnail,
        String status,
        Integer totalSections,
        Integer totalLectures,
        String createdAt,
        String updatedAt,
        List<SectionWithLectures> sections
) {}