package itmo.blps.blps.service;

import itmo.blps.blps.exception.CourseEnrollmentException;
import itmo.blps.blps.model.*;
import itmo.blps.blps.repository.CourseRepository;
import itmo.blps.blps.repository.PaymentHistoryRepository;
import itmo.blps.blps.repository.UserCourseRoleRepository;
import itmo.blps.blps.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseEnrollmentService {
        private final CourseRepository courseRepository;
        private final UserCourseRoleRepository userCourseRoleRepository;
        private final UserRepository userRepository;
        private final PaymentHistoryRepository paymentHistoryRepository;
        private final Random random = new Random();

        @Transactional(readOnly = true)
        public void checkCourseAvailability(Long courseId) {
                Course course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new CourseEnrollmentException(
                                                "COURSE_NOT_FOUND",
                                                "The requested course does not exist"));

                if (!course.isAvailable()) {
                        throw new CourseEnrollmentException(
                                        "COURSE_CLOSED",
                                        "This course is currently not available for enrollment");
                }

                if (course.getMaxStudents() != null && course.getCurrentStudents() >= course.getMaxStudents()) {
                        throw new CourseEnrollmentException(
                                        "COURSE_FULL",
                                        "This course has reached its maximum capacity");
                }
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
        public void enrollInCourse(Long userId, Long courseId) {
                // First check if the user is already enrolled
                if (userCourseRoleRepository.existsByUserIdAndCourseId(userId, courseId)) {
                        throw new CourseEnrollmentException(
                                        "ALREADY_ENROLLED",
                                        "User is already enrolled in this course");
                }

                Course course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new CourseEnrollmentException(
                                                "COURSE_NOT_FOUND",
                                                "The requested course does not exist"));

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new CourseEnrollmentException(
                                                "USER_NOT_FOUND",
                                                "User not found"));

                if (!course.isAvailable()) {
                        throw new CourseEnrollmentException(
                                        "COURSE_CLOSED",
                                        "This course is currently not available for enrollment");
                }

                // Check if user has STUDENT role
                if (user.getRole() != Role.STUDENT) {
                        throw new CourseEnrollmentException(
                                        "INVALID_ROLE",
                                        "Only students can enroll in courses");
                }

                // Проверка на максимальное количество студентов
                if (course.getMaxStudents() != null && course.getCurrentStudents() >= course.getMaxStudents()) {
                        throw new CourseEnrollmentException(
                                        "COURSE_FULL",
                                        "This course has reached its maximum capacity");
                }

                // Сохраняем платеж и получаем его объект
                PaymentHistory payment = null;
                if (course.getPrice() != null && course.getPrice() > 0) {
                        payment = processPayment(user, course);
                }

                course = courseRepository.findById(courseId).orElseThrow();

                // Check that increment was successful
                int rowsAffected = courseRepository.incrementPlaceCount(courseId);
                if (rowsAffected <= 0) {
                        if (payment != null) {
                                refundPayment(payment);
                        }
                        throw new CourseEnrollmentException(
                                        "UPDATE_FAILED",
                                        "Failed to increment course place count. Transaction will be rolled back.");
                }

                UserCourseRole enrollment = new UserCourseRole();
                enrollment.setUser(user);
                enrollment.setCourse(course);
                userCourseRoleRepository.save(enrollment);
        }

        @Transactional
        public void disenrollFromCourse(Long userId, Long courseId) {
                UserCourseRole enrollment = userCourseRoleRepository.findByUserIdAndCourseId(userId, courseId)
                                .orElseThrow(() -> new CourseEnrollmentException(
                                                "NOT_ENROLLED",
                                                "User is not enrolled in this course"));

                userCourseRoleRepository.delete(enrollment);

                int rowsAffected = courseRepository.decrementPlaceCount(courseId);
                if (rowsAffected <= 0) {
                        log.warn("Failed to decrement place count for course {}", courseId);
                }

                log.info("User {} disenrolled from course {}", userId, courseId);
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
        public PaymentHistory processPayment(User user, Course course) {
                log.info("Processing payment of {} for user {} for course {}",
                                course.getPrice(), user.getUsername(), course.getTitle());

                PaymentHistory payment = new PaymentHistory();
                payment.setUser(user);
                payment.setCourse(course);
                payment.setAmount(course.getPrice());
                payment.setCreatedAt(LocalDateTime.now());
                payment.setStatus(PaymentStatus.PROCESSING);

                if (random.nextInt(100) < 20) {
                        log.warn("Payment rejected for user {} for course {}",
                                        user.getUsername(), course.getTitle());
                        throw new CourseEnrollmentException(
                                        "PAYMENT_REJECTED",
                                        "Payment rejected by the bank");
                }

                payment.setStatus(PaymentStatus.SUCCESSFUL);
                payment.setCompletedAt(LocalDateTime.now());
                paymentHistoryRepository.save(payment);
                log.info("Payment successfully processed for user {} for course {}",
                                user.getUsername(), course.getTitle());

                return payment;
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
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
