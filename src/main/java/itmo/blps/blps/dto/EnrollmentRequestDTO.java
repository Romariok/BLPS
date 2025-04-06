package itmo.blps.blps.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class EnrollmentRequestDTO {
    @Schema(description = "Course ID to enroll in", example = "1", required = true)
    private Long courseId;
}
