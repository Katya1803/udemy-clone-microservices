package com.app.course.repository;

import com.app.course.entity.LectureProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectureProgressRepository extends JpaRepository<LectureProgress, String> {

    Optional<LectureProgress> findByEnrollmentIdAndLectureId(String enrollmentId, String lectureId);

    List<LectureProgress> findByEnrollmentId(String enrollmentId);

    @Query("SELECT lp FROM LectureProgress lp WHERE lp.enrollment.accountId = :accountId AND lp.lecture.id = :lectureId")
    Optional<LectureProgress> findByAccountIdAndLectureId(
            @Param("accountId") String accountId,
            @Param("lectureId") String lectureId
    );

    @Query("SELECT COUNT(lp) FROM LectureProgress lp WHERE lp.enrollment.id = :enrollmentId AND lp.isCompleted = true")
    long countCompletedByEnrollmentId(@Param("enrollmentId") String enrollmentId);

    @Query("SELECT COUNT(lp) FROM LectureProgress lp WHERE lp.enrollment.id = :enrollmentId")
    long countByEnrollmentId(@Param("enrollmentId") String enrollmentId);

    void deleteByEnrollmentId(String enrollmentId);
}