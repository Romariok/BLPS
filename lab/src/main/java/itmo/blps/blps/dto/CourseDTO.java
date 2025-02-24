package itmo.blps.blps.dto;

import lombok.Data;

@Data
public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private boolean available;
}
