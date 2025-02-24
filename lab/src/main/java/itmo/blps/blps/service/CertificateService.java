package itmo.blps.blps.service;

import itmo.blps.blps.dto.CertificateRequestResponseDTO;
import itmo.blps.blps.dto.CertificateRequestListDTO;
import itmo.blps.blps.dto.CertificateDecisionDTO;
import itmo.blps.blps.exception.TaskOperationException;
import itmo.blps.blps.model.*;
import itmo.blps.blps.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificateService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final TaskSubmissionRepository submissionRepository;
    private final CertificateRequestRepository certificateRequestRepository;

    @Transactional
    public CertificateRequestResponseDTO requestCertificate(Long userId, Long courseId) {
        // Check if user already requested certificate
        if (certificateRequestRepository.existsByStudentIdAndCourseId(userId, courseId)) {
            throw new TaskOperationException(
                "DUPLICATE_REQUEST",
                "Certificate was already requested for this course"
            );
        }

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new TaskOperationException(
                "COURSE_NOT_FOUND",
                "Course not found"
            ));

        User student = userRepository.findById(userId)
            .orElseThrow(() -> new TaskOperationException(
                "USER_NOT_FOUND",
                "User not found"
            ));

        // Verify course completion
        boolean allTasksCompleted = course.getTasks().stream()
            .allMatch(task -> {
                TaskSubmission submission = submissionRepository
                    .findTopByStudentIdAndTaskIdOrderBySubmittedAtDesc(userId, task.getId())
                    .orElse(null);
                
                return submission != null && 
                       submission.getScore() != null && 
                       submission.getScore() >= task.getMaxScore() * 0.6; // 60% passing threshold
            });

        if (!allTasksCompleted) {
            throw new TaskOperationException(
                "INCOMPLETE_COURSE",
                "Not all tasks are completed with passing score"
            );
        }

        // Save certificate request
        CertificateRequest request = new CertificateRequest();
        request.setStudent(student);
        request.setCourse(course);
        request.setRequestedAt(LocalDateTime.now());
        request.setStatus(CertificateRequestStatus.IN_PROGRESS);
        certificateRequestRepository.save(request);

        return new CertificateRequestResponseDTO(
            true,
            "Certificate request submitted successfully",
            "IN_PROGRESS"
        );
    }

    public CertificateRequestResponseDTO checkRequestStatus(Long userId, Long courseId) {
        CertificateRequest request = certificateRequestRepository.findByStudentIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new TaskOperationException(
                    "REQUEST_NOT_FOUND",
                    "Certificate request not found"
                ));

        return new CertificateRequestResponseDTO(
            true,
            "Certificate request status retrieved",
            request.getStatus().toString()
        );
    }

    public List<CertificateRequestListDTO> getPendingRequests(Long courseId) {
        return certificateRequestRepository.findByCourseIdAndStatus(courseId, CertificateRequestStatus.IN_PROGRESS)
            .stream()
            .map(request -> {
                CertificateRequestListDTO dto = new CertificateRequestListDTO();
                dto.setRequestId(request.getId());
                dto.setStudentId(request.getStudent().getId());
                dto.setStudentUsername(request.getStudent().getUsername());
                dto.setCourseId(request.getCourse().getId());
                dto.setCourseName(request.getCourse().getTitle());
                dto.setStatus(request.getStatus().toString());
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public CertificateRequestResponseDTO processCertificateRequest(CertificateDecisionDTO decision) {
        CertificateRequest request = certificateRequestRepository.findById(decision.getRequestId())
            .orElseThrow(() -> new TaskOperationException(
                "REQUEST_NOT_FOUND",
                "Certificate request not found"
            ));

        if (request.getStatus() != CertificateRequestStatus.IN_PROGRESS) {
            throw new TaskOperationException(
                "INVALID_STATUS",
                "Can only process requests that are in progress"
            );
        }

        request.setStatus(decision.isApproved() 
            ? CertificateRequestStatus.APPROVED 
            : CertificateRequestStatus.REJECTED);
        
        certificateRequestRepository.save(request);

        return new CertificateRequestResponseDTO(
            true,
            decision.isApproved() ? "Certificate request approved" : "Certificate request rejected",
            request.getStatus().toString()
        );
    }
} 