package itmo.blps.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_course_roles", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "course_id" }))
public class UserCourseRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}
