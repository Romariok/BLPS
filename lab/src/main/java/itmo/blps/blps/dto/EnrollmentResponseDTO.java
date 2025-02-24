package itmo.blps.blps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EnrollmentResponseDTO {
    private boolean success;
    private String message;
} 