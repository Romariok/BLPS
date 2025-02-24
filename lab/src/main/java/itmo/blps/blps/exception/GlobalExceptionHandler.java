package itmo.blps.blps.exception;

import itmo.blps.blps.dto.EnrollmentResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CourseEnrollmentException.class)
    public ResponseEntity<EnrollmentResponseDTO> handleCourseEnrollmentException(CourseEnrollmentException ex) {
        return ResponseEntity.ok(new EnrollmentResponseDTO(false, ex.getMessage()));
    }
} 