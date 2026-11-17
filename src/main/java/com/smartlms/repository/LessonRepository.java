package com.smartlms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartlms.entity.Lesson;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    // Get all lessons of a course
    List<Lesson> findByCourseIdOrderByLessonOrderAsc(Long courseId);

    // Get only active lessons of a course
    List<Lesson> findByCourseIdAndActiveTrueOrderByLessonOrderAsc(Long courseId);

    // Check lesson title inside a course
    boolean existsByCourseIdAndTitleIgnoreCase(Long courseId, String title);
}