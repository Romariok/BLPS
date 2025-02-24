package itmo.blps.blps.dto;

import lombok.Data;

@Data
public class UnscoredSubmissionDTO {
    private Long submissionId;
    private Long taskId;
    private Long studentId;
    private String studentUsername;
    private String answer;
    private Integer maxScore;
} 