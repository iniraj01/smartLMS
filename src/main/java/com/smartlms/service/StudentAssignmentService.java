
package com.smartlms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartlms.entity.Assignment;
import com.smartlms.entity.Enrollment;
import com.smartlms.repository.AssignmentRepository;

@Service
@Transactional(readOnly = true)
public class StudentAssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final EnrollmentService enrollmentService;

    public StudentAssignmentService(
            AssignmentRepository assignmentRepository,
            EnrollmentService enrollmentService) {

        this.assignmentRepository = assignmentRepository;
        this.enrollmentService = enrollmentService;
    }

    // Get published assignments for student's active enrollments
    public List<Assignment> getStudentAssignments(Long studentId) {

        // Find student's active enrollments
        List<Enrollment> enrollments =
                enrollmentService.getActiveEnrollments(studentId);

        // If student has no active enrollments
        if (enrollments.isEmpty()) {
            return List.of();
        }

        // Extract course IDs from enrollments
        List<Long> courseIds = enrollments.stream()
                .map(enrollment -> enrollment.getCourse().getId())
                .distinct()
                .toList();

        // Fetch only published assignments from those courses
        return assignmentRepository
                .findByCourseIdInAndActiveTrue(courseIds);
    }
}
