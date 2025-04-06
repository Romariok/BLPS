package itmo.blps.blps.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class TaskSubmissionDTO {
    @Schema(description = "Task ID to submit an answer for", example = "1", required = true)
    private Long taskId;
    
    @Schema(description = "Student's answer to the task", required = true)
    private String answer;
}
