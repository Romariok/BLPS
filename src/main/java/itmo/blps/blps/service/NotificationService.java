package itmo.blps.blps.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import itmo.blps.blps.model.Role;
import itmo.blps.blps.model.User;
import itmo.blps.blps.repository.TaskSubmissionRepository;
import itmo.blps.blps.repository.UserRepository;

@Service
public class NotificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JcaEmailService emailService;

    @Autowired
    private TaskSubmissionRepository taskSubmissionRepository;

    @Scheduled(fixedRate = 30000, initialDelay = 10000) // 0.5 minutes
    public void notificateUser() {
        java.time.LocalDateTime cutoffTime = java.time.LocalDateTime.now().minusMinutes(1);
        List<User> inactiveUsers = userRepository.findInactiveUsers(cutoffTime);
        for (User user : inactiveUsers) {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("username", user.getUsername());
            templateModel.put("lastActiveTime", user.getLastActiveTime());

            emailService.sendTemplateMessage(
                    user.getEmail(),
                    "We Miss You! - Come Back to Learning",
                    "user-inactivity",
                    templateModel);
        }
    }

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
}
