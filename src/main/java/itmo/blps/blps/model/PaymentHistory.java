package itmo.blps.blps.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment_history")
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistory {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @ManyToOne
   @JoinColumn(name = "user_id", nullable = false)
   private User user;

   @ManyToOne
   @JoinColumn(name = "course_id", nullable = false)
   private Course course;

   @Column(nullable = false)
   private Double amount;

   @Column(name = "created_at", nullable = false)
   private LocalDateTime createdAt;

   @Column(name = "completed_at")
   private LocalDateTime completedAt;

   @Enumerated(EnumType.STRING)
   @Column(nullable = false)
   private PaymentStatus status;

   @Override
   public int hashCode() {
      return id.hashCode();
   }
}