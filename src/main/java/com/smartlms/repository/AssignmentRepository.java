
package com.smartlms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartlms.entity.Assignment;

public interface AssignmentRepository
        extends JpaRepository<Assignment, Long> {

    // Get assignments by teacher
    List<Assignment> findByTeacherId(Long teacherId);

    // Get assignments by course
    List<Assignment> findByCourseId(Long courseId);

    // Get active assignments
    List<Assignment> findByActiveTrue();

    // Get active assignments by course
    List<Assignment> findByCourseIdAndActiveTrue(Long courseId);

    // Find assignment by ID and teacher
    Optional<Assignment> findByIdAndTeacherId(
            Long id, Long teacherId);
    List<Assignment> findByCourseIdInAndActiveTrue(List<Long> courseIds);
    

}
