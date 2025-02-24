package itmo.blps.blps.dto;

import itmo.blps.blps.model.TaskType;
import lombok.Data;

@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private TaskType type;
    private Integer maxScore;
}
