package itmo.blps.blps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CertificateRequestResponseDTO {
    private boolean success;
    private String message;
    private String status;
}