package itmo.blps.service;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.camunda.bpm.engine.RuntimeService;
import java.util.Map;
import java.util.HashMap;

import itmo.blps.dto.CertificateRequestJmsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificateJmsService {

    private final RuntimeService runtimeService;

    @JmsListener(destination = "${jms.queue.name}")
    public void processCertificateRequest(CertificateRequestJmsDTO request) {
        log.info("Received certificate request for processing - UserId: {}, CourseId: {}",
                request.getUserId(), request.getCourseId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("userId", request.getUserId());
        variables.put("courseId", request.getCourseId());

        runtimeService.startProcessInstanceByMessage("MsgStartCertificateProcess", variables);

        log.info("BPMN process 'MsgStartCertificateProcess' initiated for user {} and course {}",
                request.getUserId(), request.getCourseId());
    }
}