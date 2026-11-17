
package com.smartlms.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Course;
import com.smartlms.entity.Lesson;
import com.smartlms.entity.LessonProgress;
import com.smartlms.repository.EnrollmentRepository;
import com.smartlms.repository.LessonProgressRepository;
import com.smartlms.repository.LessonRepository;
import com.smartlms.repository.UserRepository;

@Service
public class LessonProgressService {

    private final LessonProgressRepository progressRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    public LessonProgressService(
            LessonProgressRepository progressRepository,
            LessonRepository lessonRepository,
            UserRepository userRepository,
            EnrollmentRepository enrollmentRepository) {

        this.progressRepository = progressRepository;
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    // Mark lesson as completed
    @Transactional
    public void markLessonComplete(Long studentId, Long lessonId) {

        AppUser student = userRepository.findById(studentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found!"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Lesson not found!"));

        Course course = lesson.getCourse();

        boolean enrolled = enrollmentRepository
                .existsByStudentIdAndCourseId(studentId, course.getId());

        if (!enrolled) {
            throw new IllegalArgumentException(
                    "You are not enrolled in this course!");
        }

        if (!lesson.isActive()) {
            throw new IllegalArgumentException(
                    "This lesson is not published!");
        }

        LessonProgress progress = progressRepository
                .findByStudentIdAndLessonId(studentId, lessonId)
                .orElseGet(() -> {
                    LessonProgress newProgress = new LessonProgress();
                    newProgress.setStudent(student);
                    newProgress.setLesson(lesson);
                    return newProgress;
                });

        // Avoid resetting completion date on repeated clicks
        if (!progress.isCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());

            progressRepository.save(progress);
        }
    }

    // Get progress of a student in a course
    @Transactional(readOnly = true)
    public List<LessonProgress> getCourseProgress(
            Long studentId, Long courseId) {

        return progressRepository
                .findByStudentIdAndLessonCourseId(studentId, courseId);
    }

    // Count completed lessons
    @Transactional(readOnly = true)
    public long getCompletedLessons(
            Long studentId, Long courseId) {

        return progressRepository
                .countByStudentIdAndLessonCourseIdAndCompletedTrue(
                        studentId, courseId);
    }

    // Calculate course completion percentage
    @Transactional(readOnly = true)
    public int getCourseProgressPercentage(
            Long studentId, Long courseId) {

        long totalLessons = lessonRepository
                .findByCourseIdAndActiveTrueOrderByLessonOrderAsc(courseId)
                .size();

        if (totalLessons == 0) {
            return 0;
        }

        long completedLessons =
                getCompletedLessons(studentId, courseId);

        return (int) Math.round(
                (completedLessons * 100.0) / totalLessons);
    }
}
