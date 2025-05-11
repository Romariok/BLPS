package itmo.blps.blps.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

@Data
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    private boolean available;

    @Column(name = "min_score")
    private Integer minimumScore;

    @Column(name = "max_students")
    private Integer maxStudents;

    @Column(name = "current_students")
    private Integer currentStudents = 0;

    @Column(name = "price")
    private Double price;

    @OneToMany(mappedBy = "course")
    private Set<UserCourseRole> userRoles;

    @OneToMany(mappedBy = "course")
    private Set<Task> tasks;
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
