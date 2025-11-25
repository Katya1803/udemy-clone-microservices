package com.app.course.repository;

import com.app.course.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, String> {

    List<Lecture> findBySectionIdOrderBySortOrderAsc(String sectionId);

    @Query("SELECT l FROM Lecture l WHERE l.section.id = :sectionId ORDER BY l.sortOrder ASC")
    List<Lecture> findLecturesBySectionId(@Param("sectionId") String sectionId);

    @Query("SELECT l FROM Lecture l WHERE l.section.course.id = :courseId ORDER BY l.section.sortOrder ASC, l.sortOrder ASC")
    List<Lecture> findLecturesByCourseId(@Param("courseId") String courseId);

    @Query("SELECT COUNT(l) FROM Lecture l WHERE l.section.id = :sectionId")
    long countBySectionId(@Param("sectionId") String sectionId);

    @Query("SELECT COUNT(l) FROM Lecture l WHERE l.section.course.id = :courseId")
    long countByCourseId(@Param("courseId") String courseId);

    void deleteBySectionId(String sectionId);
}