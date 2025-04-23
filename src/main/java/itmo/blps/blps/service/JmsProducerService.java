package itmo.blps.blps.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import itmo.blps.blps.dto.CertificateRequestJmsDTO;
import jakarta.jms.Queue;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JmsProducerService {
    @Autowired
    private JmsTemplate jmsTemplate;
    
    @Autowired
    private Queue queue;

    public void sendMessage(CertificateRequestJmsDTO certificateRequest) {
        try {
            log.info("Sending JMS message for certificate generation");
            
            jmsTemplate.convertAndSend(queue, certificateRequest);
            
            log.info("JMS message sent successfully");
        } catch (JmsException e) {
            log.error("Failed to send JMS message", e);
            throw new RuntimeException("Failed to send message to queue", e);
        }
    }
} 