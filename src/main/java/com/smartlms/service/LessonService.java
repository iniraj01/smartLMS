package com.smartlms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartlms.entity.Course;
import com.smartlms.entity.Lesson;
import com.smartlms.repository.CourseRepository;
import com.smartlms.repository.LessonRepository;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    public LessonService(LessonRepository lessonRepository,
                         CourseRepository courseRepository) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
    }

    // Get all lessons of a course
    @Transactional(readOnly = true)
    public List<Lesson> getLessonsByCourse(Long courseId) {
        return lessonRepository.findByCourseIdOrderByLessonOrderAsc(courseId);
    }

    // Get only published lessons
    @Transactional(readOnly = true)
    public List<Lesson> getActiveLessons(Long courseId) {
        return lessonRepository.findByCourseIdAndActiveTrueOrderByLessonOrderAsc(courseId);
    }

    // Add new lesson
    public Lesson addLesson(Long courseId, String title,
                            String description, String videoUrl,
                            Integer lessonOrder) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found!"));

        if (lessonRepository.existsByCourseIdAndTitleIgnoreCase(courseId, title)) {
            throw new IllegalArgumentException("Lesson title already exists in this course!");
        }

        Lesson lesson = new Lesson();
        lesson.setTitle(title);
        lesson.setDescription(description);
        lesson.setVideoUrl(videoUrl);
        lesson.setLessonOrder(lessonOrder);
        lesson.setCourse(course);

        return lessonRepository.save(lesson);
    }

    // Update lesson
    public Lesson updateLesson(Long lessonId, String title,
                               String description, String videoUrl,
                               Integer lessonOrder) {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found!"));

        lesson.setTitle(title);
        lesson.setDescription(description);
        lesson.setVideoUrl(videoUrl);
        lesson.setLessonOrder(lessonOrder);

        return lessonRepository.save(lesson);
    }

    // Publish / Unpublish lesson
    public void updateLessonStatus(Long lessonId, boolean active) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found!"));

        lesson.setActive(active);
        lessonRepository.save(lesson);
    }

    // Delete lesson
    public void deleteLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found!"));

        lessonRepository.delete(lesson);
    }
}