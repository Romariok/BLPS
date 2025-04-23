package itmo.blps.service;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import itmo.blps.dto.CertificateRequestJmsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificateProcessorService {

    private final CertificateService certificateService;

    @JmsListener(destination = "${jms.queue.name}")
    public void processCertificateRequest(CertificateRequestJmsDTO request) {
        log.info("Received certificate request for processing - UserId: {}, CourseId: {}", 
                request.getUserId(), request.getCourseId());
        
        certificateService.generateAndSendCertificate(request.getUserId(), request.getCourseId());
        
        log.info("Certificate processing completed for user {} and course {}", 
                request.getUserId(), request.getCourseId());
    }
} 