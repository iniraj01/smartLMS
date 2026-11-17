
package com.smartlms.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.smartlms.entity.QuestionOption;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {

    List<QuestionOption> findByQuestionId(Long questionId);

    long countByQuestionId(Long questionId);
}
