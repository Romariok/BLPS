package itmo.blps.blps.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class CertificateRequestListDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long requestId;
    private Long studentId;
    private String studentUsername;
    private Long courseId;
    private String courseName;
    private String status;

    @Override
    public String toString() {
        return "Студентик {" +
                "requestId=" + requestId +
                ", studentUsername='" + studentUsername + '\'' +
                ", courseName='" + courseName + '\'' +
                '}';
    }
}