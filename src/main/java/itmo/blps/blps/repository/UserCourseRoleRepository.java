package itmo.blps.blps.repository;

import itmo.blps.blps.model.UserCourseRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserCourseRoleRepository extends JpaRepository<UserCourseRole, Long> {
    List<UserCourseRole> findByUserId(Long userId);

    Optional<UserCourseRole> findByUserIdAndCourseId(Long userId, Long courseId);

    List<UserCourseRole> findByCourseId(Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}