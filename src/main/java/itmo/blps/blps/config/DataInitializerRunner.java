package itmo.blps.blps.config;

import itmo.blps.blps.model.*;
import itmo.blps.blps.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializerRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserCourseRoleRepository userCourseRoleRepository;
    private final TaskRepository taskRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;
    private final CertificateRequestRepository certificateRequestRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Profile("!test") // Don't run in test profile to avoid conflicts with test data setup
    @DependsOn("rolePermissionInitializer") // Ensure role permissions are loaded first
    public CommandLineRunner initTestData() {
        return args -> {
            // Check if data already exists to avoid duplicate inserts
            if (userRepository.count() > 0) {
                log.info("Test data already exists. Skipping initialization.");
                return;
            }

            log.info("Initializing test data...");
            loadTestData();
            log.info("Test data initialized successfully!");
        };
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner rolePermissionInitializerRunner() {
        return args -> {
            // RolePermissionInitializer's @PostConstruct method will handle role
            // permissions
            log.info("Role permissions will be initialized by RolePermissionInitializer...");
        };
    }

    @Transactional
    public void loadTestData() {
        try {
            // Create users with roles
            User student1 = createUser("student1", "affi7@ya.ru", Role.STUDENT, LocalDateTime.now().minusSeconds(30));
            User student2 = createUser("student2", "affi7@yandex.by", Role.STUDENT,LocalDateTime.now().minusSeconds(45));
            User student3 = createUser("student3", "affi7@yandex.com", Role.STUDENT,LocalDateTime.now().minusSeconds(60));
            User teacher1 = createUser("teacher1", "affi7@yandex.ru", Role.TEACHER,LocalDateTime.now().minusSeconds(75));
            createUser("admin", "affi7@yandex.kz", Role.TEACHER,LocalDateTime.now().minusSeconds(90));

            // Create courses
            Course javaCourse = createCourse(
                    "Java Programming Basics",
                    "Learn the fundamentals of Java programming language",
                    true,
                    70,
                    null,
                    null);

            Course webCourse = createCourse(
                    "Advanced Web Development",
                    "Master modern web development techniques",
                    true,
                    75,
                    null,
                    null);

            Course dataScienceCourse = createCourse(
                    "Data Science Fundamentals",
                    "Introduction to data analysis and machine learning",
                    false,
                    80,
                    null,
                    null);

            Course limitedCourse = createCourse(
                    "Limited Enrollment Course",
                    "This is a premium course with limited enrollment",
                    true,
                    75,
                    2,
                    99.99);

            // Associate users with courses
            associateUserWithCourse(student3, limitedCourse);
            associateUserWithCourse(teacher1, limitedCourse);
            associateUserWithCourse(student1, javaCourse);
            associateUserWithCourse(student1, webCourse);
            associateUserWithCourse(student2, javaCourse);
            associateUserWithCourse(teacher1, javaCourse);
            associateUserWithCourse(teacher1, webCourse);
            associateUserWithCourse(teacher1, dataScienceCourse);

            // Create tasks for Java course
            Task javaVariablesTask = createTask(
                    "Java Variables Quiz",
                    "Select the correct data type for storing decimal numbers",
                    TaskType.MULTIPLE_CHOICE,
                    javaCourse,
                    "double",
                    10);

            Task javaControlStructuresTask = createTask(
                    "Java Control Structures",
                    "Select all valid loop types in Java",
                    TaskType.CHECKBOX,
                    javaCourse,
                    "for,while,do-while",
                    15);

            Task javaOopTask = createTask(
                    "Java OOP Concepts",
                    "Explain the principles of Object-Oriented Programming",
                    TaskType.WRITTEN,
                    javaCourse,
                    null,
                    25);

            // Create tasks for Web Development course
            createTask(
                    "HTML Elements",
                    "Which tag is used for creating hyperlinks?",
                    TaskType.MULTIPLE_CHOICE,
                    webCourse,
                    "a",
                    10);

            createTask(
                    "CSS Selectors",
                    "Select all valid CSS selectors",
                    TaskType.CHECKBOX,
                    webCourse,
                    "class,id,tag,attribute",
                    15);

            createTask(
                    "JavaScript Project",
                    "Create a simple to-do list application using JavaScript",
                    TaskType.WRITTEN,
                    webCourse,
                    null,
                    30);

            // Create tasks for Data Science course
            createTask(
                    "Statistical Measures",
                    "Which measure represents the middle value in a dataset?",
                    TaskType.MULTIPLE_CHOICE,
                    dataScienceCourse,
                    "median",
                    10);

            createTask(
                    "Data Visualization",
                    "Select all appropriate chart types for categorical data",
                    TaskType.CHECKBOX,
                    dataScienceCourse,
                    "bar,pie,heatmap",
                    15);

            createTask(
                    "Machine Learning Analysis",
                    "Analyze the provided dataset using a classification algorithm",
                    TaskType.WRITTEN,
                    dataScienceCourse,
                    null,
                    40);

            // Create task submissions for Student1
            createTaskSubmission(
                    student1,
                    javaVariablesTask,
                    "double",
                    10,
                    LocalDateTime.now().minusDays(5),
                    LocalDateTime.now().minusDays(4),
                    null,
                    true);

            createTaskSubmission(
                    student1,
                    javaControlStructuresTask,
                    "for,while,do-while",
                    15,
                    LocalDateTime.now().minusDays(5),
                    LocalDateTime.now().minusDays(4),
                    null,
                    true);

            createTaskSubmission(
                    student1,
                    javaOopTask,
                    "Object-Oriented Programming is based on four main principles: Encapsulation, Inheritance, Polymorphism, and Abstraction.",
                    20,
                    LocalDateTime.now().minusDays(4),
                    LocalDateTime.now().minusDays(2),
                    teacher1,
                    false);

            // Create task submissions for Student2
            createTaskSubmission(
                    student2,
                    javaVariablesTask,
                    "float",
                    5,
                    LocalDateTime.now().minusDays(3),
                    LocalDateTime.now().minusDays(2),
                    null,
                    true);

            createTaskSubmission(
                    student2,
                    javaControlStructuresTask,
                    "for,while",
                    10,
                    LocalDateTime.now().minusDays(3),
                    LocalDateTime.now().minusDays(2),
                    null,
                    true);

            createTaskSubmission(
                    student2,
                    javaOopTask,
                    "OOP is a programming paradigm that uses objects and classes.",
                    null,
                    LocalDateTime.now().minusDays(1),
                    null,
                    null,
                    false);

            // Create certificate requests
            createCertificateRequest(
                    student2,
                    javaCourse,
                    LocalDateTime.now().minusDays(1),
                    CertificateRequestStatus.IN_PROGRESS);

        } catch (Exception e) {
            log.error("Error initializing test data", e);
        }
    }

    private User createUser(String username, String email, Role role, LocalDateTime lastActiveTime) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User user = new User();
                    user.setUsername(username);
                    user.setPassword(passwordEncoder.encode("password"));
                    user.setRole(role);
                    user.setEmail(email);
                    user.setLastActiveTime(lastActiveTime);
                    return userRepository.save(user);
                });
    }

    private Course createCourse(String title, String description, boolean available, int minScore,
            Integer maxStudents, Double price) {
        // Since findByTitle is not available, we need to find the course by iterating
        for (Course course : courseRepository.findAll()) {
            if (course.getTitle().equals(title)) {
                return course;
            }
        }

        Course course = new Course();
        course.setTitle(title);
        course.setDescription(description);
        course.setAvailable(available);
        course.setMinimumScore(minScore);
        course.setMaxStudents(maxStudents);
        course.setPrice(price);
        course.setCurrentStudents(0);
        return courseRepository.save(course);
    }

    private void associateUserWithCourse(User user, Course course) {
        if (userCourseRoleRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
            return;
        }

        UserCourseRole userCourseRole = new UserCourseRole();
        userCourseRole.setUser(user);
        userCourseRole.setCourse(course);

        if (user.getRole() == Role.STUDENT) {
            course.setCurrentStudents(course.getCurrentStudents() + 1);
            courseRepository.save(course);
        }

        userCourseRoleRepository.save(userCourseRole);
    }

    private Task createTask(String title, String description, TaskType type, Course course, String correctAnswer,
            int maxScore) {
        // Since findByTitleAndCourseId is not available, we need to find the task by
        // iterating
        for (Task task : taskRepository.findByCourseId(course.getId())) {
            if (task.getTitle().equals(title)) {
                return task;
            }
        }

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setType(type);
        task.setCourse(course);
        task.setCorrectAnswer(correctAnswer);
        task.setMaxScore(maxScore);
        return taskRepository.save(task);
    }

    private void createTaskSubmission(User user, Task task, String answer, Integer score,
            LocalDateTime submittedAt, LocalDateTime gradedAt,
            User gradedBy, boolean automaticallyGraded) {
        // Check if submission already exists
        if (!taskSubmissionRepository.findByStudentIdAndTaskId(user.getId(), task.getId()).isEmpty()) {
            // If a submission already exists, we won't create a new one
            return;
        }

        TaskSubmission submission = new TaskSubmission();
        submission.setStudent(user);
        submission.setTask(task);
        submission.setAnswer(answer);
        submission.setScore(score);
        submission.setSubmittedAt(submittedAt);
        submission.setGradedAt(gradedAt);
        submission.setTeacher(gradedBy);
        submission.setAutomaticallyGraded(automaticallyGraded);
        taskSubmissionRepository.save(submission);
    }

    private void createCertificateRequest(User user, Course course, LocalDateTime requestedAt,
            CertificateRequestStatus status) {
        // Check if certificate request already exists
        if (certificateRequestRepository.existsByStudentIdAndCourseId(user.getId(), course.getId())) {
            return;
        }

        CertificateRequest request = new CertificateRequest();
        request.setStudent(user);
        request.setCourse(course);
        request.setRequestedAt(requestedAt);
        request.setStatus(status);
        certificateRequestRepository.save(request);
    }
}