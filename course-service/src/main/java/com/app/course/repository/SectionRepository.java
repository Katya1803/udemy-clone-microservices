package com.app.course.repository;

import com.app.course.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, String> {

    List<Section> findByCourseIdOrderBySortOrderAsc(String courseId);

    @Query("SELECT s FROM Section s WHERE s.course.id = :courseId ORDER BY s.sortOrder ASC")
    List<Section> findSectionsByCourseId(@Param("courseId") String courseId);

    @Query("SELECT COUNT(s) FROM Section s WHERE s.course.id = :courseId")
    long countByCourseId(@Param("courseId") String courseId);

    void deleteByCourseId(String courseId);
}