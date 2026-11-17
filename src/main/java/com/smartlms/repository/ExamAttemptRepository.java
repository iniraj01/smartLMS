
package com.smartlms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartlms.entity.ExamAttempt;
import com.smartlms.entity.ExamResultStatus;

public interface ExamAttemptRepository
        extends JpaRepository<ExamAttempt, Long> {

    List<ExamAttempt> findByStudentIdAndExamIdOrderByAttemptNumberDesc(
            Long studentId, Long examId);

    Optional<ExamAttempt> findByIdAndStudentId(
            Long id, Long studentId);

    long countByStudentIdAndExamId(
            Long studentId, Long examId);

    boolean existsByStudentIdAndExamIdAndResultStatus(
            Long studentId,
            Long examId,
            ExamResultStatus resultStatus);

    // Student exam history
    List<ExamAttempt> findByStudentIdOrderByStartedAtDesc(
            Long studentId);
    List<ExamAttempt> findByExamIdOrderByStartedAtDesc(Long examId);
}
