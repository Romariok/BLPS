package itmo.blps.blps.exception;

import itmo.blps.blps.dto.EnrollmentResponseDTO;
import itmo.blps.blps.dto.TaskSubmissionResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CourseEnrollmentException.class)
    public ResponseEntity<EnrollmentResponseDTO> handleCourseEnrollmentException(CourseEnrollmentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new EnrollmentResponseDTO(false, ex.getMessage()));
    }

    @ExceptionHandler(TaskOperationException.class)
    public ResponseEntity<TaskSubmissionResponseDTO> handleTaskOperationException(TaskOperationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new TaskSubmissionResponseDTO(false, ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidScoreException.class)
    public ResponseEntity<TaskSubmissionResponseDTO> handleInvalidScoreException(InvalidScoreException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new TaskSubmissionResponseDTO(false, ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidAnswerException.class)
    public ResponseEntity<TaskSubmissionResponseDTO> handleInvalidAnswerException(InvalidAnswerException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new TaskSubmissionResponseDTO(false, ex.getMessage(), null));
    }
}
