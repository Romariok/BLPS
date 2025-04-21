package itmo.blps.blps.repository;

import itmo.blps.blps.model.TaskSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {
    List<TaskSubmission> findByStudentIdAndTaskCourseId(Long studentId, Long courseId);

    List<TaskSubmission> findByTaskIdAndAutomaticallyGradedFalse(Long taskId);

    List<TaskSubmission> findByTaskIdAndAutomaticallyGradedFalseAndGradedAtIsNull(Long taskId);

    List<TaskSubmission> findByStudentIdAndTaskId(Long studentId, Long taskId);

    Long countByGradedAtIsNull();

    Optional<TaskSubmission> findTopByStudentIdAndTaskIdOrderBySubmittedAtDesc(Long studentId, Long taskId);
}
