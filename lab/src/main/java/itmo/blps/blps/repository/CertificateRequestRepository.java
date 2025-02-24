package itmo.blps.blps.repository;

import itmo.blps.blps.model.CertificateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CertificateRequestRepository extends JpaRepository<CertificateRequest, Long> {
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    Optional<CertificateRequest> findByStudentIdAndCourseId(Long studentId, Long courseId);
} 