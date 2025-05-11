package itmo.blps.blps.bpms;

import java.util.Set;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import itmo.blps.blps.model.CertificateRequest;
import itmo.blps.blps.model.Course;
import itmo.blps.blps.model.Permission;
import itmo.blps.blps.repository.CertificateRequestRepository;
import itmo.blps.blps.repository.CourseRepository;
import itmo.blps.blps.repository.TaskSubmissionRepository;
import itmo.blps.blps.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("certServ")
public class CertificateServiceBpms {
    private static final float threshold = 0.6f;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskSubmissionRepository submissionRepository;
    @Autowired
    private CertificateRequestRepository certificateRequestRepository;

    public void isExistingCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new BpmnError("404");
        }
        ;
    }

    @SuppressWarnings("unchecked")
    public void checkAuthority(DelegateExecution execution) {
        Set<String> authorities = (Set<String>) execution.getVariable("authorities");
        if (authorities == null || !authorities.contains(Permission.VIEW_CERTIFICATE.toString())) {
            log.info("\n\n\n\n\nAUTHORITIES: ");
            if (authorities != null) {
                authorities.forEach(auth -> log.info(auth));
            }
            log.info("\n\n\n\n\n");
            throw new BpmnError("403");
        }
    }

    public boolean areAllTaskDone(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Unchecked existence of course"));

        return new java.util.HashSet<>(course.getTasks())
                .stream()
                .allMatch(task -> {
                    var submission = submissionRepository
                            .findTopByStudentIdAndTaskIdOrderBySubmittedAtDesc(userId, task.getId())
                            .orElse(null);

                    return submission != null && submission.getScore() != null
                            && submission.getScore() >= task.getMaxScore() * threshold;
                });
    }

    public void createCertificateRequest(Long userId, Long courseId) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Unchecked existance of course"));

        var student = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Unchecked existance of user"));
        CertificateRequest certificateRequest = new CertificateRequest();
        certificateRequest.setCourse(course);
        certificateRequest.setStudent(student);
        certificateRequest.setRequestedAt(java.time.LocalDateTime.now());
        certificateRequest.setStatus(itmo.blps.blps.model.CertificateRequestStatus.IN_PROGRESS);
        certificateRequestRepository.save(certificateRequest);
    }
}
