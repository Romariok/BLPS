package itmo.blps.blps.repository;

import itmo.blps.blps.model.Role;
import itmo.blps.blps.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
    
    List<User> findByRole(Role role);
    
    @Query("SELECT u FROM User u JOIN u.courseRoles cr WHERE cr.role = 'TEACHER'")
    List<User> findAllTeachers();
}