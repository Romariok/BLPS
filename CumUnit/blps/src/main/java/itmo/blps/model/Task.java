package itmo.blps.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType type;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private String correctAnswer; // For automatic checking (multiple choice/checkbox)

    private Integer maxScore;
}
