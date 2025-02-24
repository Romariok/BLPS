package itmo.blps.blps.exception;

import lombok.Getter;

@Getter
public class TaskOperationException extends RuntimeException {
    private final String reason;

    public TaskOperationException(String reason, String message) {
        super(message);
        this.reason = reason;
    }
}
