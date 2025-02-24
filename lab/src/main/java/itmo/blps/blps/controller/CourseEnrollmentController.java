package itmo.blps.blps.controller;

import itmo.blps.blps.dto.EnrollmentRequestDTO;
import itmo.blps.blps.dto.EnrollmentResponseDTO;
import itmo.blps.blps.service.CourseEnrollmentService;

import itmo.blps.blps.exception.TaskOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseEnrollmentController {
    private final CourseEnrollmentService courseEnrollmentService;

    @PostMapping("/enroll")
    public ResponseEntity<EnrollmentResponseDTO> enrollInCourse(@RequestBody EnrollmentRequestDTO request)
            throws TaskOperationException {
        courseEnrollmentService.checkCourseAvailability(request.getCourseId());
        courseEnrollmentService.enrollInCourse(request.getUserId(), request.getCourseId());
        return ResponseEntity.ok(new EnrollmentResponseDTO(true, "Successfully enrolled in course"));
    }
}
