package com.app.course.repository;

import com.app.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

    Optional<Course> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Course> findByStatus(Course.CourseStatus status, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.status = :status " +
            "AND (LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Course> searchByStatusAndKeyword(
            @Param("status") Course.CourseStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT COUNT(c) FROM Course c WHERE c.status = :status")
    long countByStatus(@Param("status") Course.CourseStatus status);
}