package itmo.blps.blps.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "certificate_requests")
public class CertificateRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    @Enumerated(EnumType.STRING)
    private CertificateRequestStatus status;

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}