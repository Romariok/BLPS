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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JcaEmailService emailService;

    @Autowired
    private TaskSubmissionRepository taskSubmissionRepository;

    @Autowired
    private UserMapper userMapper;
    /*
     * @Scheduled(fixedRate = 30000, initialDelay = 10000) // 0.5 minutes
     * public void notificateUser() {
     * List<User> inactiveUsers = getInactiveUsers();
     * for (User user : inactiveUsers) {
     * Map<String, Object> templateModel = new HashMap<>();
     * templateModel.put("username", user.getUsername());
     * templateModel.put("lastActiveTime", user.getLastActiveTime());
     * 
     * emailService.sendTemplateMessage(
     * user.getEmail(),
     * "We Miss You! - Come Back to Learning",
     * "user-inactivity",
     * templateModel);
     * }
     * }
     */

    @Scheduled(fixedRate = 30000, initialDelay = 15000) // 0.5 minutes
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
        String email = user.getEmail();
        String username = user.getUsername();
        LocalDateTime lastActiveTime = user.getLastActiveTime();
        String templateName = "user-inactivity";
        String subject = "We Miss You! - Come Back to Learning";
        String message = "Hello " + username + ",\n\n" +
                "We noticed that you haven't been active on our platform since " + lastActiveTime + ".\n" +
                "We miss you and would love to see you back!\n\n" +
                "Best regards,\n" +
                "Your Learning Platform Team";
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("username", username);
        templateModel.put("lastActiveTime", lastActiveTime);
        templateModel.put("message", message);
        emailService.sendTemplateMessage(email, subject, templateName, templateModel);
    }
}
