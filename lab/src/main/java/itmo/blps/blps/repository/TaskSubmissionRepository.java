package itmo.blps.blps.repository;

import itmo.blps.blps.model.TaskSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {
    List<TaskSubmission> findByStudentIdAndTaskCourseId(Long studentId, Long courseId);

    List<TaskSubmission> findByTaskIdAndAutomaticallyGradedFalse(Long taskId);
}