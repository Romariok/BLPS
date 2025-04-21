package itmo.blps.blps.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;
import jakarta.resource.spi.security.PasswordCredential;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Set;
import javax.security.auth.Subject;
import java.util.Objects;

@ConnectionDefinition(connectionFactory = SmtpConnectionFactory.class, connectionFactoryImpl = SmtpConnectionFactoryImpl.class, connection = SmtpConnection.class, connectionImpl = SmtpConnectionImpl.class)
public class SmtpManagedConnectionFactory implements ManagedConnectionFactory, ResourceAdapterAssociation {

    private static final long serialVersionUID = 1L;

    private String host;
    private int port;
    private String username;
    private String password;
    private String protocol;
    private String senderEmail;
    private PrintWriter logWriter;
    private SmtpResourceAdapter resourceAdapter;

    @Override
    public Object createConnectionFactory(ConnectionManager cm) throws ResourceException {
        return new SmtpConnectionFactoryImpl(this, cm);
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        return new SmtpConnectionFactoryImpl(this, null);
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cri)
            throws ResourceException {

        String userName = this.username;
        String password = this.password;

        if (subject != null) {
            PasswordCredential pc = Utility.getPasswordCredential(this, subject, cri);
            if (pc != null) {
                userName = pc.getUserName();
                password = new String(pc.getPassword());
            }
        }

        if (cri != null && cri instanceof SmtpConnectionRequestInfo) {
            SmtpConnectionRequestInfo smtpCri = (SmtpConnectionRequestInfo) cri;
            if (smtpCri.getUsername() != null) {
                userName = smtpCri.getUsername();
            }
            if (smtpCri.getPassword() != null) {
                password = smtpCri.getPassword();
            }
        }

        Properties mailProps = new Properties();
        mailProps.put("mail.transport.protocol", protocol);

        mailProps.put("mail.smtp.host", host);
        mailProps.put("mail.smtp.port", String.valueOf(port));
        mailProps.put("mail.smtp.auth", "true");
        mailProps.put("mail.smtp.ssl.enable", "true");
        mailProps.put("mail.smtp.starttls.enable", "true");
        mailProps.put("mail.smtp.user", userName);
        mailProps.put("mail.smtp.password", password);
        mailProps.put("mail.debug", "true");

        return new SmtpManagedConnection(this, userName, password, mailProps);
    }

    @Override
    public ManagedConnection matchManagedConnections(@SuppressWarnings("rawtypes") Set connections, Subject subject,
            ConnectionRequestInfo cri) throws ResourceException {

        PasswordCredential pc = null;
        if (subject != null) {
            pc = Utility.getPasswordCredential(this, subject, cri);
        }

        for (Object connection : connections) {
            if (connection instanceof SmtpManagedConnection managedConn) {

                if (managedConn.getFactory().equals(this)) {
                    if (pc == null || managedConn.passwordMatches(pc.getUserName(), new String(pc.getPassword()))) {
                        return managedConn;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return this.logWriter;
    }

    @Override
    public ResourceAdapter getResourceAdapter() {
        return this.resourceAdapter;
    }

    @Override
    public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
        if (ra != null && !(ra instanceof SmtpResourceAdapter)) {
            throw new ResourceException("Invalid resource adapter provided");
        }
        this.resourceAdapter = (SmtpResourceAdapter) ra;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SmtpManagedConnectionFactory that = (SmtpManagedConnectionFactory) o;

        return port == that.port &&
                Objects.equals(host, that.host) &&
                Objects.equals(username, that.username) &&
                Objects.equals(protocol, that.protocol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, username, protocol);
    }
}