package com.smartlms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartlms.entity.Enrollment;
import com.smartlms.entity.EnrollmentStatus;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Get all enrollments of a student
    List<Enrollment> findByStudentId(Long studentId);

    // Get active enrollments of a student
    List<Enrollment> findByStudentIdAndStatus(
            Long studentId, EnrollmentStatus status);

    // Get all students enrolled in a course
    List<Enrollment> findByCourseId(Long courseId);

    // Check whether student is already enrolled
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    // Count students enrolled in a course
    long countByCourseIdAndStatus(
            Long courseId, EnrollmentStatus status);
}