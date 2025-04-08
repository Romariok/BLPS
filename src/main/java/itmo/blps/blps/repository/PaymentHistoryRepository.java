package itmo.blps.blps.repository;

import itmo.blps.blps.model.PaymentHistory;
import itmo.blps.blps.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
   List<PaymentHistory> findByUserId(Long userId);

   List<PaymentHistory> findByCourseId(Long courseId);

   List<PaymentHistory> findByUserIdAndCourseId(Long userId, Long courseId);

   List<PaymentHistory> findByStatus(PaymentStatus status);
}