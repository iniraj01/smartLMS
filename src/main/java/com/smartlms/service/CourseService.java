
package com.smartlms.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.smartlms.entity.Course;
import com.smartlms.repository.CourseRepository;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    // Get all courses
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // Get active courses
    public List<Course> getActiveCourses() {
        return courseRepository.findByActiveTrue();
    }

    // Get course by ID
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found!"));
    }

    // Create course
    public Course createCourse(Course course) {

        if (courseRepository.existsByTitleIgnoreCase(course.getTitle())) {
            throw new IllegalArgumentException("Course already exists!");
        }

        return courseRepository.save(course);
    }

    // Update course
    public Course updateCourse(Long id, Course updatedCourse) {

        Course existingCourse = getCourseById(id);

        boolean duplicate = courseRepository
                .existsByTitleIgnoreCase(updatedCourse.getTitle());

        if (duplicate && !existingCourse.getTitle().equalsIgnoreCase(updatedCourse.getTitle())) {
            throw new IllegalArgumentException("Course title already exists!");
        }

        existingCourse.setTitle(updatedCourse.getTitle());
        existingCourse.setDescription(updatedCourse.getDescription());
        existingCourse.setCategory(updatedCourse.getCategory());
        existingCourse.setDuration(updatedCourse.getDuration());
        existingCourse.setPrice(updatedCourse.getPrice());

        return courseRepository.save(existingCourse);
    }

    // Activate / Deactivate course
    public void updateCourseStatus(Long id, boolean active) {

        Course course = getCourseById(id);
        course.setActive(active);

        courseRepository.save(course);
    }

    // Delete course
    public void deleteCourse(Long id) {

        Course course = getCourseById(id);
        courseRepository.delete(course);
    }
}
