package itmo.blps.blps.repository;

import itmo.blps.blps.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}