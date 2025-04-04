package itmo.blps.blps.exception;

import lombok.Getter;

@Getter
public class InvalidScoreException extends RuntimeException {
    private final String reason;

    public InvalidScoreException(String reason, String message) {
        super(message);
        this.reason = reason;
    }
}