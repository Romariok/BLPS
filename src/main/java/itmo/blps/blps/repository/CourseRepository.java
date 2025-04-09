package itmo.blps.blps.repository;

import itmo.blps.blps.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {
    
    @Modifying
    @Query("UPDATE Course c SET c.currentStudents = c.currentStudents - 1 WHERE c.id = :courseId AND c.currentStudents > 0")
    int decrementPlaceCount(@Param("courseId") Long courseId);
    
    @Modifying
    @Query("UPDATE Course c SET c.currentStudents = c.currentStudents + 1 WHERE c.id = :courseId AND (c.maxStudents IS NULL OR c.currentStudents < c.maxStudents)")
    int incrementPlaceCount(@Param("courseId") Long courseId);
}