package com.app.course.repository;

import com.app.course.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {

    Optional<Enrollment> findByAccountIdAndCourseId(String accountId, String courseId);

    boolean existsByAccountIdAndCourseId(String accountId, String courseId);

    Page<Enrollment> findByAccountId(String accountId, Pageable pageable);

    Page<Enrollment> findByCourseId(String courseId, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId")
    long countByCourseId(@Param("courseId") String courseId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.accountId = :accountId")
    long countByAccountId(@Param("accountId") String accountId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.accountId = :accountId AND e.completedAt IS NOT NULL")
    long countCompletedByAccountId(@Param("accountId") String accountId);
}