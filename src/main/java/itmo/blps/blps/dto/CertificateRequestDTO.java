package itmo.blps.blps.dto;

import lombok.Data;

@Data
public class CertificateRequestDTO {
    private Long userId;
    private Long courseId;
}