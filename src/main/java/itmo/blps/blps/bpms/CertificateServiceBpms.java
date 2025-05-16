package itmo.blps.blps.bpms;

import java.util.Set;
import java.util.stream.Collectors;

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
import itmo.blps.blps.service.JmsProducerService;
import itmo.blps.blps.dto.CertificateRequestJmsDTO;
import itmo.blps.blps.dto.CertificateRequestResponseDTO;
import itmo.blps.blps.mapper.CertificateMapper;

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
    @Autowired
    private CertificateMapper certificateMapper;
    @Autowired
    private JmsProducerService jmsProducerService;


    public void isExistingCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new BpmnError("404");
        }
    }

    public void isExistingRequest(Long courseId, Long userId) {
        if (!certificateRequestRepository.existsByStudentIdAndCourseId(userId, courseId)) {
            throw new BpmnError("404");
        }
    }

    public void isExistingRequest(Long requestId) {
        if (!certificateRequestRepository.existsById(requestId)) {
            throw new BpmnError("404");
        }
    }

    public void isRequestInProgress(Long requestId) {
        if (certificateRequestRepository.findById(requestId).get().getStatus() != itmo.blps.blps.model.CertificateRequestStatus.IN_PROGRESS) {
            throw new BpmnError("400");
        }
    }

    @SuppressWarnings("unchecked")
    public void checkAuthority(DelegateExecution execution) {
        Set<String> authorities = (Set<String>) execution.getVariable("authorities");
        if (authorities == null || !authorities.contains(Permission.VIEW_CERTIFICATE.toString())) {
            throw new BpmnError("403");
        }
    }

    @SuppressWarnings("unchecked")
    public void checkTeacherAuthority(DelegateExecution execution) {
        Set<String> authorities = (Set<String>) execution.getVariable("authorities");
        if (authorities == null || !authorities.contains(Permission.ISSUE_CERTIFICATE.toString())) {
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

    public String getPendingRequests(Long courseId) {
        return certificateRequestRepository
                        .findByCourseIdAndStatus(courseId, itmo.blps.blps.model.CertificateRequestStatus.IN_PROGRESS)
                        .stream()
                        .map(certificateMapper::toCertificateRequestListDTO)
                        .collect(Collectors.toList()).toString();
    }

    public String checkRequestStatus(Long userId, Long courseId) {
        var request = certificateRequestRepository.findByStudentIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new BpmnError("404"));

        return request.getStatus().toString();
    }

    public void requestCertificateGeneration(Long userId, Long courseId) {
        CertificateRequestJmsDTO requestDto = new CertificateRequestJmsDTO();
        requestDto.setUserId(userId);
        requestDto.setCourseId(courseId);
        jmsProducerService.sendMessage(requestDto);
    }

    public String processCertificateRequest(Boolean decision, Long requestId) {
        CertificateRequest request = certificateRequestRepository.findById(requestId)
                        .orElseThrow(() -> new BpmnError("404"));

        if (request.getStatus() != itmo.blps.blps.model.CertificateRequestStatus.IN_PROGRESS) {
                throw new BpmnError("400");
        }

        request.setStatus(decision
                        ? itmo.blps.blps.model.CertificateRequestStatus.APPROVED
                        : itmo.blps.blps.model.CertificateRequestStatus.REJECTED);

        certificateRequestRepository.save(request);
        requestCertificateGeneration(request.getStudent().getId(),request.getCourse().getId());
        return decision ? "Certificate request approved" : "Certificate request rejected";
}
}
