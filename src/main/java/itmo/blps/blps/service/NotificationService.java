package itmo.blps.blps.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import itmo.blps.blps.dto.UserDTO;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import itmo.blps.blps.mapper.UserMapper;
import itmo.blps.blps.model.Role;
import itmo.blps.blps.model.User;
import itmo.blps.blps.repository.TaskSubmissionRepository;
import itmo.blps.blps.repository.UserRepository;

import java.time.LocalDateTime;

@Service("notificationService")
public class NotificationService {

    private static final long INACTIVITY_THRESHOLD_MINUTES = 1;
    private static final String userTemplateName = "user-inactivity";
    private static final String teacherTemplateName = "teacher-pending-tasks";
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JcaEmailService emailService;

    @Autowired
    private TaskSubmissionRepository taskSubmissionRepository;

    @Autowired
    private UserMapper userMapper;

    public long getUnscoredTasksCount() {
        return taskSubmissionRepository.countByGradedAtIsNull();
    }

    @Scheduled(fixedRate = 30000, initialDelay = 15000000) // 0.5 minutes
    public void notificateTeacher() {
        long ungradedTasks = taskSubmissionRepository.countByGradedAtIsNull();
        if (ungradedTasks > 5) {
            List<User> teachers = userRepository.findByRole(Role.TEACHER);

            for (User teacher : teachers) {
                Map<String, Object> templateModel = new HashMap<>();
                templateModel.put("username", teacher.getUsername());
                templateModel.put("taskCount", ungradedTasks);

                emailService.sendTemplateMessage(
                        teacher.getEmail(),
                        "Tasks Waiting for Review",
                        "teacher-pending-tasks",
                        templateModel);
            }
        }
    }

    public void getAllTeachers(DelegateExecution execution) {
        List<UserDTO> allTeachers = userRepository.findByRole(Role.TEACHER).stream().map(userMapper::userToUserDTO)
                .toList();
        execution.setVariable("listTeacher", allTeachers);
    }

    public void getAllStudents(DelegateExecution execution) {
        List<UserDTO> allStudents = userRepository.findByRole(Role.STUDENT).stream().map(userMapper::userToUserDTO)
                .toList();
        execution.setVariable("listStudent", allStudents);
    }

    public boolean checkInactiveUsers(DelegateExecution execution) {
        UserDTO user = (UserDTO) execution.getVariable("currentStudent");
        LocalDateTime lastActiveTime = user.getLastActiveTime();
        LocalDateTime currentTime = LocalDateTime.now();
        long inactiveDuration = java.time.Duration.between(lastActiveTime, currentTime).toMinutes();
        return inactiveDuration > INACTIVITY_THRESHOLD_MINUTES;
    }

    public void sendNotification(DelegateExecution execution) {
        UserDTO user = (UserDTO) execution.getVariable("currentStudent");
        String subject = "We Miss You! - Come Back to Learning";
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("username", user.getUsername());
        templateModel.put("lastActiveTime", user.getLastActiveTime());
        emailService.sendTemplateMessage(user.getEmail(), subject, userTemplateName, templateModel);
    }

    public void sendTeacherNotification(DelegateExecution execution) {
        UserDTO teacher = (UserDTO) execution.getVariable("currentTeacher");
        long ungradedTasks = (Long) execution.getVariable("unscoredCount");
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("username", teacher.getUsername());
        templateModel.put("taskCount", ungradedTasks);

        emailService.sendTemplateMessage(
                teacher.getEmail(),
                "Tasks Waiting for Review",
                teacherTemplateName,
                templateModel);
    }
}
