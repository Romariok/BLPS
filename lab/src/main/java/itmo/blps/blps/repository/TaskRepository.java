package itmo.blps.blps.repository;

import itmo.blps.blps.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCourseId(Long courseId);
}