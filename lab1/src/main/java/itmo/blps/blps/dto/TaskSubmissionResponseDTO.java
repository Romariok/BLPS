package itmo.blps.blps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskSubmissionResponseDTO {
    private boolean success;
    private String message;
    private Integer score; // Will be null for non-automatic tasks
}
