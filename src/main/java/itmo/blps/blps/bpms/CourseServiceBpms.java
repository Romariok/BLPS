package itmo.blps.blps.bpms;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;

import itmo.blps.blps.model.Course;
import itmo.blps.blps.model.PaymentHistory;
import itmo.blps.blps.model.PaymentStatus;
import itmo.blps.blps.model.Permission;
import itmo.blps.blps.model.User;
import itmo.blps.blps.model.UserCourseRole;
import itmo.blps.blps.repository.CourseRepository;
import itmo.blps.blps.repository.PaymentHistoryRepository;
import itmo.blps.blps.repository.UserCourseRoleRepository;
import itmo.blps.blps.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Service("courseServ")
@Slf4j
public class CourseServiceBpms {
   @Autowired
   private CourseRepository courseRepository;
   @Autowired
   private UserRepository userRepository;
   @Autowired
   private UserCourseRoleRepository userCourseRoleRepository;
   @Autowired
   private PaymentHistoryRepository paymentHistoryRepository;

   @SuppressWarnings("unchecked")
   public void checkBaseCourseAuthority(DelegateExecution execution) {
      Set<String> authorities = (Set<String>) execution.getVariable("authorities");
      if (authorities == null || !authorities.contains(Permission.VIEW_COURSE.toString())) {
         throw new BpmnError("403");
      }
   }

   public void checkCourseAvailability(Long courseId) {
      Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BpmnError("404"));

      if (!course.isAvailable()) {
         throw new BpmnError("400");
      }

      if (course.getMaxStudents() != null && course.getCurrentStudents() >= course.getMaxStudents()) {
         throw new BpmnError("409");
      }
   }

   public void enrollInCourse(Long userId, Long courseId) {
      Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BpmnError("500"));

      User user = userRepository.findById(userId)
            .orElseThrow(() -> new BpmnError("500"));

      // Сохраняем платеж и получаем его объект
      PaymentHistory payment = null;
      if (course.getPrice() != null && course.getPrice() > 0) {
         payment = processPayment(user, course);
      }

      course = courseRepository.findById(courseId).orElseThrow();

      int rowsAffected = courseRepository.incrementPlaceCount(courseId);
      if (rowsAffected <= 0) {
         if (payment != null) {
            refundPayment(payment);
         }
         throw new BpmnError("409");
      }

      UserCourseRole enrollment = new UserCourseRole();
      enrollment.setUser(user);
      enrollment.setCourse(course);
      userCourseRoleRepository.save(enrollment);
   }

   public PaymentHistory processPayment(User user, Course course) {
      log.info("Processing payment of {} for user {} for course {}",
            course.getPrice(), user.getUsername(), course.getTitle());

      PaymentHistory payment = new PaymentHistory();
      payment.setUser(user);
      payment.setCourse(course);
      payment.setAmount(course.getPrice());
      payment.setCreatedAt(LocalDateTime.now());
      payment.setStatus(PaymentStatus.PROCESSING);
      if (Math.random() < 0.5) {
         log.info("Payment declined for user {} for course {}", user.getUsername(), course.getTitle());
         payment.setStatus(PaymentStatus.FAILED);
         payment.setCompletedAt(LocalDateTime.now());
         paymentHistoryRepository.save(payment);
         throw new BpmnError("402");
      }
      payment.setStatus(PaymentStatus.SUCCESSFUL);
      payment.setCompletedAt(LocalDateTime.now());
      paymentHistoryRepository.save(payment);
      log.info("Payment successfully processed for user {} for course {}",
            user.getUsername(), course.getTitle());

      return payment;
   }

   public void disenrollFromCourse(Long userId, Long courseId) {
      UserCourseRole enrollment = userCourseRoleRepository.findByUserIdAndCourseId(userId, courseId)
            .orElseThrow(() -> new BpmnError("400"));

      userCourseRoleRepository.delete(enrollment);

      int rowsAffected = courseRepository.decrementPlaceCount(courseId);
      if (rowsAffected <= 0) {
         log.warn("Failed to decrement place count for course {}", courseId);
      }

      log.info("User {} disenrolled from course {}", userId, courseId);
   }

   public void refundPayment(PaymentHistory payment) {
      User user = payment.getUser();
      Course course = payment.getCourse();

      log.info("Refunding payment of {} for user {} for course {}",
            course.getPrice(), user.getUsername(), course.getTitle());

      try {
         payment.setStatus(PaymentStatus.RESTORED);
         payment.setCompletedAt(LocalDateTime.now());
         paymentHistoryRepository.save(payment);
         log.info("Payment successfully refunded for user {} for course {}",
               user.getUsername(), course.getTitle());
      } catch (Exception e) {
         log.error("Error refunding payment for user {} for course {}: {}",
               user.getUsername(), course.getTitle(), e.getMessage());
         throw e;
      }
   }

}
