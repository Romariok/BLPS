package itmo.blps.blps.service;

import itmo.blps.blps.dto.CertificateRequestResponseDTO;
import itmo.blps.blps.exception.TaskOperationException;
import itmo.blps.blps.model.*;
import itmo.blps.blps.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

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
} 