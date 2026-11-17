
package com.smartlms.repository;
import java.util.List;
import com.smartlms.entity.Exam;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.smartlms.entity.Exam;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    Optional<Exam> findByCourseId(Long courseId);

    Optional<Exam> findByIdAndTeacherId(Long id, Long teacherId);

    boolean existsByCourseId(Long courseId);
    List<Exam> findByCourseIdInAndActiveTrue(List<Long> courseIds);
}
