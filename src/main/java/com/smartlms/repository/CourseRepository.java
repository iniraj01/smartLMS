
package com.smartlms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartlms.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByActiveTrue();

    List<Course> findByCategory(String category);

    boolean existsByTitleIgnoreCase(String title);
    List<Course> findByTeacherId(Long teacherId);
    List<Course> findByTeacherEmail(String email);
}
