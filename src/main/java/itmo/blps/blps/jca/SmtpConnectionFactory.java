package itmo.blps.blps.jca;

import jakarta.resource.ResourceException;

public interface SmtpConnectionFactory {
    SmtpConnection getConnection() throws ResourceException;
    SmtpConnection getConnection(String username, String password) throws ResourceException;
} 