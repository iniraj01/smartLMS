
package com.smartlms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartlms.entity.AssignmentSubmission;
import com.smartlms.entity.SubmissionStatus;

public interface AssignmentSubmissionRepository
        extends JpaRepository<AssignmentSubmission, Long> {

    // Get all submissions made by a student
    List<AssignmentSubmission> findByStudentId(Long studentId);

    // Get submissions for a particular assignment
    List<AssignmentSubmission> findByAssignmentId(Long assignmentId);

    // Get a particular student's submission for an assignment
    Optional<AssignmentSubmission> findByStudentIdAndAssignmentId(
            Long studentId, Long assignmentId);

    // Check whether student has already submitted
    boolean existsByStudentIdAndAssignmentId(
            Long studentId, Long assignmentId);

    // Get all submissions received by a teacher
    List<AssignmentSubmission> findByAssignment_Teacher_Id(Long teacherId);

    // Get submissions by their status
    List<AssignmentSubmission> findByStudentIdAndStatus(
            Long studentId, SubmissionStatus status);

    // Count submissions for an assignment
    long countByAssignmentId(Long assignmentId);
}
