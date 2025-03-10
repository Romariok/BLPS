package itmo.blps.blps.dto;

import lombok.Data;

@Data
public class CertificateRequestListDTO {
    private Long requestId;
    private Long studentId;
    private String studentUsername;
    private Long courseId;
    private String courseName;
    private String status;
} 