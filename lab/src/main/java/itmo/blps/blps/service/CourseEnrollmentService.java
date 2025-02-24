package itmo.blps.blps.service;

import itmo.blps.blps.exception.CourseEnrollmentException;
import itmo.blps.blps.model.*;
import itmo.blps.blps.repository.CourseRepository;
import itmo.blps.blps.repository.UserCourseRoleRepository;
import itmo.blps.blps.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseEnrollmentService {
    private final CourseRepository courseRepository;
    private final UserCourseRoleRepository userCourseRoleRepository;
    private final UserRepository userRepository;

    public void checkCourseAvailability(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseEnrollmentException(
                        "COURSE_NOT_FOUND",
                        "The requested course does not exist"
                ));

        if (!course.isAvailable()) {
            throw new CourseEnrollmentException(
                    "COURSE_CLOSED",
                    "This course is currently not available for enrollment"
            );
        }
    }

    @Transactional
    public void enrollInCourse(Long userId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseEnrollmentException(
                        "COURSE_NOT_FOUND",
                        "The requested course does not exist"
                ));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CourseEnrollmentException(
                        "USER_NOT_FOUND",
                        "User not found"
                ));

        if (!course.isAvailable()) {
            throw new CourseEnrollmentException(
                    "COURSE_CLOSED",
                    "This course is currently not available for enrollment"
            );
        }

        UserCourseRole enrollment = new UserCourseRole();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setRole(Role.STUDENT);
        userCourseRoleRepository.save(enrollment);
    }

    public boolean isCourseAvailable(Long courseId) {
        return courseRepository.findById(courseId)
                .map(Course::isAvailable)
                .orElse(false);
    }
}
