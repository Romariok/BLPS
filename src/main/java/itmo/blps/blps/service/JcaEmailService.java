package itmo.blps.blps.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import itmo.blps.blps.jca.SmtpConnection;
import jakarta.resource.ResourceException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Service
@Slf4j
public class JcaEmailService {

    private final SmtpConnection smtpConnection;
    private final TemplateEngine templateEngine;
    
    @Value("${sender.email}")
    private String senderEmail;
    
    @Autowired
    public JcaEmailService(SmtpConnection smtpConnection, TemplateEngine templateEngine) {
        this.smtpConnection = smtpConnection;
        this.templateEngine = templateEngine;
    }
    
    public void sendEmail(String to, String subject, String text) {
        try {
            smtpConnection.sendEmail(to, subject, text);
            log.info("Email sent to {}", to);
        } catch (ResourceException e) {
            log.error("Failed to send email to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    public void sendEmailWithAttachment(String to, String subject, String text, String attachmentPath) {
        try {
            smtpConnection.sendEmailWithAttachment(to, subject, text, attachmentPath);
            log.info("Email with attachment sent to {}", to);
        } catch (ResourceException e) {
            log.error("Failed to send email with attachment to {}", to, e);
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }
    
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            smtpConnection.sendEmail(to, subject, htmlContent);
            log.info("HTML email sent to {}", to);
        } catch (ResourceException e) {
            log.error("Failed to send HTML email to {}", to, e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }
    
    public void sendTemplateMessage(String to, String subject, String templateName, Map<String, Object> templateModel) {
        try {
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);
            
            String htmlBody = templateEngine.process(templateName, thymeleafContext);
            
            sendHtmlEmail(to, subject, htmlBody);
            log.info("Template email sent to {} using template {}", to, templateName);
        } catch (Exception e) {
            log.error("Failed to send template email to {}", to, e);
            throw new RuntimeException("Failed to send template email", e);
        }
    }
}