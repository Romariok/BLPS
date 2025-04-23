package itmo.blps.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import itmo.blps.jca.SmtpConnection;
import itmo.blps.jca.SmtpConnectionFactory;
import itmo.blps.jca.SmtpManagedConnectionFactory;
import itmo.blps.jca.SmtpResourceAdapter;

import jakarta.resource.ResourceException;
import org.springframework.jca.support.ResourceAdapterFactoryBean;

@Configuration
public class JcaConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.protocol}")
    private String protocol;

    @Value("${sender.email}")
    private String senderEmail;

    @Bean
    public ResourceAdapterFactoryBean resourceAdapter() {
        ResourceAdapterFactoryBean bean = new ResourceAdapterFactoryBean();
        bean.setResourceAdapter(new SmtpResourceAdapter());
        return bean;
    }

    @Bean
    @Primary
    public SmtpManagedConnectionFactory smtpManagedConnectionFactory() {
        SmtpManagedConnectionFactory mcf = new SmtpManagedConnectionFactory();
        mcf.setHost(host);
        mcf.setPort(port);
        mcf.setUsername(username);
        mcf.setPassword(password);
        mcf.setProtocol(protocol);
        mcf.setSenderEmail(senderEmail);
        return mcf;
    }

    @Bean
    public SmtpConnectionFactory smtpConnectionFactory(
            SmtpManagedConnectionFactory managedConnectionFactory) {
        return new SmtpConnectionFactory(managedConnectionFactory, null);
    }

    @Bean
    public SmtpConnection smtpConnection(SmtpConnectionFactory connectionFactory)
            throws ResourceException {

        return connectionFactory.getConnection();
    }
}