package itmo.blps.blps.exception;

import lombok.Getter;

@Getter
public class InvalidAnswerException extends RuntimeException {
    private final String reason;

    public InvalidAnswerException(String reason, String message) {
        super(message);
        this.reason = reason;
    }
} 