package com.app.course.dto.section;

import com.app.course.dto.lecture.LectureListItem;

import java.util.List;

public record SectionWithLectures(
        String id,
        String title,
        Integer sortOrder,
        List<LectureListItem> lectures
) {}