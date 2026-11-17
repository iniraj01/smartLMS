
package com.smartlms.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartlms.entity.AppUser;
import com.smartlms.entity.Assignment;
import com.smartlms.entity.Course;
import com.smartlms.entity.Role;
import com.smartlms.repository.AssignmentRepository;
import com.smartlms.repository.CourseRepository;
import com.smartlms.repository.UserRepository;

@Service
@Transactional
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public AssignmentService(
            AssignmentRepository assignmentRepository,
            CourseRepository courseRepository,
            UserRepository userRepository) {

        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    // Create Assignment
    public Assignment createAssignment(
            String title,
            String description,
            LocalDate dueDate,
            Double totalMarks,
            Long courseId,
            Long teacherId) {

        AppUser teacher = getTeacher(teacherId);
        Course course = getCourse(courseId);

        validateCourseOwnership(course, teacher);

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Assignment title is required.");
        }

        if (dueDate == null) {
            throw new IllegalArgumentException("Due date is required.");
        }

        if (totalMarks == null || totalMarks <= 0) {
            throw new IllegalArgumentException("Total marks must be greater than zero.");
        }

        Assignment assignment = new Assignment();
        assignment.setTitle(title.trim());
        assignment.setDescription(description);
        assignment.setDueDate(dueDate);
        assignment.setTotalMarks(totalMarks);
        assignment.setCourse(course);
        assignment.setTeacher(teacher);
        assignment.setActive(true);

        return assignmentRepository.save(assignment);
    }

    // Get Assignments Created By Teacher
    @Transactional(readOnly = true)
    public List<Assignment> getTeacherAssignments(Long teacherId) {

        getTeacher(teacherId);

        return assignmentRepository.findByTeacherId(teacherId);
    }

    // Get Assignments For A Course
    @Transactional(readOnly = true)
    public List<Assignment> getCourseAssignments(Long courseId) {

        return assignmentRepository.findByCourseId(courseId);
    }

    // Update Assignment
    public Assignment updateAssignment(
            Long assignmentId,
            Long teacherId,
            String title,
            String description,
            LocalDate dueDate,
            Double totalMarks) {

        AppUser teacher = getTeacher(teacherId);

        Assignment assignment = assignmentRepository
                .findByIdAndTeacherId(assignmentId, teacherId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Assignment not found."));

        validateCourseOwnership(assignment.getCourse(), teacher);

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Assignment title is required.");
        }

        if (dueDate == null) {
            throw new IllegalArgumentException("Due date is required.");
        }

        if (totalMarks == null || totalMarks <= 0) {
            throw new IllegalArgumentException("Total marks must be greater than zero.");
        }

        assignment.setTitle(title.trim());
        assignment.setDescription(description);
        assignment.setDueDate(dueDate);
        assignment.setTotalMarks(totalMarks);

        return assignmentRepository.save(assignment);
    }

    // Publish / Unpublish Assignment
    public void updateAssignmentStatus(
            Long assignmentId,
            Long teacherId,
            boolean active) {

        AppUser teacher = getTeacher(teacherId);

        Assignment assignment = assignmentRepository
                .findByIdAndTeacherId(assignmentId, teacherId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Assignment not found."));

        validateCourseOwnership(assignment.getCourse(), teacher);

        assignment.setActive(active);
        assignmentRepository.save(assignment);
    }

    // Delete Assignment
    public void deleteAssignment(Long assignmentId, Long teacherId) {

        AppUser teacher = getTeacher(teacherId);

        Assignment assignment = assignmentRepository
                .findByIdAndTeacherId(assignmentId, teacherId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Assignment not found."));

        validateCourseOwnership(assignment.getCourse(), teacher);

        assignmentRepository.delete(assignment);
    }

    // Validate Teacher
    private AppUser getTeacher(Long teacherId) {

        AppUser teacher = userRepository.findById(teacherId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher not found."));

        if (teacher.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("User is not a teacher.");
        }

        return teacher;
    }

    // Find Course
    private Course getCourse(Long courseId) {

        return courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Course not found."));
    }

    // Check Whether Course Belongs To Teacher
    private void validateCourseOwnership(Course course, AppUser teacher) {

        if (course.getTeacher() == null ||
                !course.getTeacher().getId().equals(teacher.getId())) {

            throw new IllegalArgumentException(
                    "You are not assigned to this course.");
        }
    }
}
