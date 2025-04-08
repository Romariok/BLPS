package itmo.blps.blps.service;

import itmo.blps.blps.exception.CourseEnrollmentException;
import itmo.blps.blps.model.*;
import itmo.blps.blps.repository.CourseRepository;
import itmo.blps.blps.repository.UserCourseRoleRepository;
import itmo.blps.blps.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseEnrollmentService {
        private final CourseRepository courseRepository;
        private final UserCourseRoleRepository userCourseRoleRepository;
        private final UserRepository userRepository;

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

        @Transactional
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

                
                if (course.getPrice() != null && course.getPrice() > 0) {
                        processPayment(user, course);
                }

                course = courseRepository.findById(courseId).orElseThrow();
                if (course.getMaxStudents() != null && course.getCurrentStudents() >= course.getMaxStudents()) {
                        if (course.getPrice() != null && course.getPrice() > 0) {
                                refundPayment(user, course);
                        }
                        throw new CourseEnrollmentException(
                                        "COURSE_FULL",
                                        "This course has reached its maximum capacity during payment processing");
                }

                // Инкрементируем счетчик текущих студентов
                course.setCurrentStudents(course.getCurrentStudents() + 1);
                courseRepository.save(course);

                UserCourseRole enrollment = new UserCourseRole();
                enrollment.setUser(user);
                enrollment.setCourse(course);
                userCourseRoleRepository.save(enrollment);
        }

        @Transactional
        public void processPayment(User user, Course course) {
                log.info("Processing payment of {} for user {} for course {}",
                                course.getPrice(), user.getUsername(), course.getTitle());

        }

        @Transactional
        public void refundPayment(User user, Course course) {
                log.info("Refunding payment of {} for user {} for course {}",
                                course.getPrice(), user.getUsername(), course.getTitle());

        }

        public boolean isCourseAvailable(Long courseId) {
                return courseRepository.findById(courseId)
                                .map(course -> course.isAvailable() &&
                                                (course.getMaxStudents() == null ||
                                                                course.getCurrentStudents() < course.getMaxStudents()))
                                .orElse(false);
        }
}
