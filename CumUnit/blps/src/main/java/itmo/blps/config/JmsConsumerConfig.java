package itmo.blps.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;

@Configuration
@EnableJms
public class JmsConsumerConfig {

    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(
            ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer) {
        
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        factory.setSessionTransacted(true);
        configurer.configure(factory, connectionFactory);
        
        return factory;
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        
        // Add type mapping to handle different package names
        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("itmo.blps.blps.dto.CertificateRequestJmsDTO", 
                           itmo.blps.dto.CertificateRequestJmsDTO.class);
        converter.setTypeIdMappings(typeIdMappings);
        
        return converter;
    }
} 