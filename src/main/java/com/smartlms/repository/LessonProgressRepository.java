
package com.smartlms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartlms.entity.LessonProgress;

@Repository
public interface LessonProgressRepository
        extends JpaRepository<LessonProgress, Long> {

    // Find progress of a particular student and lesson
    Optional<LessonProgress> findByStudentIdAndLessonId(
            Long studentId, Long lessonId);

    // Get all progress records of a student
    List<LessonProgress> findByStudentId(Long studentId);

    // Get progress records of a student for a particular course
    List<LessonProgress> findByStudentIdAndLessonCourseId(
            Long studentId, Long courseId);

    // Count completed lessons in a course
    long countByStudentIdAndLessonCourseIdAndCompletedTrue(
            Long studentId, Long courseId);

    // Check whether progress record exists
    boolean existsByStudentIdAndLessonId(
            Long studentId, Long lessonId);
}
