package itmo.blps.blps.jca;

import jakarta.resource.ResourceException;

public interface SmtpConnection {
    void sendEmail(String to, String subject, String body) throws ResourceException;
    void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath) throws ResourceException;
    void close() throws ResourceException;
} 