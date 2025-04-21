package itmo.blps.blps.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;
    @Value("${sender.email}")
    private String senderEmail;

    public void sendMessageWithAttachment(String to, String subject, String text, String attachmentPath) {
        try {
            MimeMessageHelper helper = createMessageHelper(to, subject);
            helper.setText(text);

            FileSystemResource file = new FileSystemResource(new File(attachmentPath));
            helper.addAttachment(file.getFilename(), file);

            emailSender.send(helper.getMimeMessage());
            log.info("Email with attachment sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email with attachment to {}", to, e);
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }

    public void sendHtmlMessage(String to, String subject, String htmlContent) {
        try {
            MimeMessageHelper helper = createMessageHelper(to, subject);
            helper.setText(htmlContent, true);

            emailSender.send(helper.getMimeMessage());
            log.info("HTML email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}", to, e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    public void sendTemplateMessage(String to, String subject, String templateName, Map<String, Object> templateModel) {
        try {
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);

            String htmlBody = templateEngine.process(templateName, thymeleafContext);

            sendHtmlMessage(to, subject, htmlBody);
            log.info("Template email sent to {} using template {}", to, templateName);
        } catch (Exception e) {
            log.error("Failed to send template email to {}", to, e);
            throw new RuntimeException("Failed to send template email", e);
        }
    }
    
    private MimeMessageHelper createMessageHelper(String to, String subject) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(senderEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        return helper;
    }
}