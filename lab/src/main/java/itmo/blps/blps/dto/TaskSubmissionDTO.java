package itmo.blps.blps.dto;

import lombok.Data;

@Data
public class TaskSubmissionDTO {
    private Long taskId;
    private Long userId;
    private String answer;
}
