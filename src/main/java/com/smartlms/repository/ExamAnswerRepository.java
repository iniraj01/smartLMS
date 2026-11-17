
package com.smartlms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartlms.entity.ExamAnswer;

public interface ExamAnswerRepository
        extends JpaRepository<ExamAnswer, Long> {

    List<ExamAnswer> findByAttemptId(Long attemptId);

    long countByAttemptId(Long attemptId);
}
