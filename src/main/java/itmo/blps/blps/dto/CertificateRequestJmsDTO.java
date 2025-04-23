package itmo.blps.blps.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CertificateRequestJmsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long courseId;
} 