package itmo.blps.blps.camunda;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import itmo.blps.blps.dto.CertificateRequestJmsDTO;
import itmo.blps.blps.service.JmsProducerService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ExternalTaskSubscription("certGenerator")
public class CertificateGenerationHandler implements ExternalTaskHandler {

    @Autowired
    private JmsProducerService jmsProducerService;

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        try {
            Long userId = externalTask.getVariable("userId");
            Long courseId = externalTask.getVariable("courseId");

            if (userId == null || courseId == null) {
                externalTaskService.handleFailure(
                        externalTask,
                        "Missing process variables",
                        "userId and/or courseId not found in process variables.",
                        0, 
                        0L);
                return;
            }
            
            log.info("Processing certificate generation for userId: {} and courseId: {} via external task id {}", userId, courseId, externalTask.getId());

            CertificateRequestJmsDTO requestDto = new CertificateRequestJmsDTO();
            requestDto.setUserId(userId);
            requestDto.setCourseId(courseId);

            jmsProducerService.sendMessage(requestDto);

            externalTaskService.complete(externalTask);
            log.info("External task {} completed successfully.", externalTask.getId());
        } catch (Exception e) {
            log.error("Error processing external task {}: {}", externalTask.getId(), e.getMessage(), e);
            externalTaskService.handleFailure(
                    externalTask,
                    "Error processing certificate generation",
                    e.getMessage(),
                    0,
                    5000L);
        }
    }
} 