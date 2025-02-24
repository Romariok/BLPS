package itmo.blps.blps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseUnavailabilityDTO {
    private String reason;
    private String message;
}
