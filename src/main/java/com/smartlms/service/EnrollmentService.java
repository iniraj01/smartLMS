package com.smartlms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Course;
import com.smartlms.entity.Enrollment;
import com.smartlms.entity.EnrollmentStatus;
import com.smartlms.entity.Role;
import com.smartlms.repository.CourseRepository;
import com.smartlms.repository.EnrollmentRepository;
import com.smartlms.repository.UserRepository;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository,
            CourseRepository courseRepository) {

        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    // Enroll student in a course
    @Transactional
    public Enrollment enrollStudent(Long studentId, Long courseId) {

        AppUser student = userRepository.findById(studentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found!"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Course not found!"));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException(
                    "Only students can enroll in courses!");
        }

        if (!student.isEnabled()) {
            throw new IllegalArgumentException(
                    "Your account is disabled!");
        }

        if (!course.isActive()) {
            throw new IllegalArgumentException(
                    "This course is not available!");
        }

        if (enrollmentRepository.existsByStudentIdAndCourseId(
                studentId, courseId)) {

            throw new IllegalArgumentException(
                    "You are already enrolled in this course!");
        }

        Enrollment enrollment = new Enrollment();

        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        return enrollmentRepository.save(enrollment);
    }

    // Get all courses enrolled by a student
    @Transactional(readOnly = true)
    public List<Enrollment> getStudentEnrollments(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    // Get active courses of a student
    @Transactional(readOnly = true)
    public List<Enrollment> getActiveEnrollments(Long studentId) {
        return enrollmentRepository.findByStudentIdAndStatus(
                studentId, EnrollmentStatus.ACTIVE);
    }

    // Get all students enrolled in a course
    @Transactional(readOnly = true)
    public List<Enrollment> getCourseEnrollments(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

 // Get only active students enrolled in a course
 @Transactional(readOnly = true)
 public List<Enrollment> getActiveEnrollmentsForCourse(Long courseId) {

     return enrollmentRepository.findByCourseId(courseId)
             .stream()
             .filter(enrollment ->
                     enrollment.getStatus() == EnrollmentStatus.ACTIVE)
             .toList();
 }

    // Count active students in a course
    @Transactional(readOnly = true)
    public long getActiveStudentCount(Long courseId) {
        return enrollmentRepository.countByCourseIdAndStatus(
                courseId, EnrollmentStatus.ACTIVE);
    }
}