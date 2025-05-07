package itmo.blps.blps.bpms;

import org.springframework.stereotype.Service;

@Service("certServ")
public class CertificateServiceBpms {
    private static final float threshold = 0.6f;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final TaskSubmissionRepository submissionRepository;
    private final CertificateRequestRepository certificateRequestRepository;
}
