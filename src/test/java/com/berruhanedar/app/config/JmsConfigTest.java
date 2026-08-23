package com.berruhanedar.app.config;

import jakarta.jms.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class JmsConfigTest {

    private JmsConfig jmsConfig;

    @BeforeEach
    void setUp() {
        jmsConfig = new JmsConfig();
    }

    @Test
    void shouldCreateJacksonJmsMessageConverter() {

        JsonMapper jsonMapper = JsonMapper.builder().build();

        MessageConverter converter =
                jmsConfig.jacksonJmsMessageConverter(jsonMapper);

        assertNotNull(converter);
        assertInstanceOf(
                JacksonJsonMessageConverter.class,
                converter
        );
    }

    @Test
    void shouldCreateJmsListenerContainerFactory() {

        ConnectionFactory connectionFactory =
                mock(ConnectionFactory.class);

        MessageConverter messageConverter =
                mock(MessageConverter.class);

        DefaultJmsListenerContainerFactory factory =
                jmsConfig.jmsListenerContainerFactory(
                        connectionFactory,
                        messageConverter
                );

        assertNotNull(factory);
    }
}