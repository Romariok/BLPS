package itmo.blps.blps.dto;

import lombok.Data;

@Data
public class CertificateDecisionDTO {
    private Long requestId;
    private boolean approved;
}