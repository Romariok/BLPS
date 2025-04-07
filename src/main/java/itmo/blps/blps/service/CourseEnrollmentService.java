package itmo.blps.blps.service;

import itmo.blps.blps.exception.CourseEnrollmentException;
import itmo.blps.blps.model.*;
import itmo.blps.blps.repository.CourseRepository;
import itmo.blps.blps.repository.UserCourseRoleRepository;
import itmo.blps.blps.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseEnrollmentService {
        private final CourseRepository courseRepository;
        private final UserCourseRoleRepository userCourseRoleRepository;
        private final UserRepository userRepository;

        @PreAuthorize("hasAuthority('VIEW_COURSE')")
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
        }

        @Transactional
        @PreAuthorize("hasAuthority('VIEW_COURSE')")
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

                UserCourseRole enrollment = new UserCourseRole();
                enrollment.setUser(user);
                enrollment.setCourse(course);
                userCourseRoleRepository.save(enrollment);
        }

        @PreAuthorize("hasAuthority('VIEW_COURSE')")
        public boolean isCourseAvailable(Long courseId) {
                return courseRepository.findById(courseId)
                                .map(Course::isAvailable)
                                .orElse(false);
        }
}
