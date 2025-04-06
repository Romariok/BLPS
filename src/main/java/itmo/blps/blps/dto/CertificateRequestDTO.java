package itmo.blps.blps.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class CertificateRequestDTO {
    @Schema(description = "Course ID to request certificate for", example = "1", required = true)
    private Long courseId;
}