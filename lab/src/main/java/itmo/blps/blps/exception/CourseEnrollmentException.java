package itmo.blps.blps.exception;

import lombok.Getter;

@Getter
public class CourseEnrollmentException extends RuntimeException {
    private final String reason;

    public CourseEnrollmentException(String reason, String message) {
        super(message);
        this.reason = reason;
    }
} 