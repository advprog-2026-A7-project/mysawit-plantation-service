package com.mysawit.plantation.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PLANTATION_EXCHANGE = "plantation.exchange";
    public static final String MANDOR_ASSIGNED_KEY   = "plantation.assignment.mandor-assigned";
    public static final String MANDOR_UNASSIGNED_KEY = "plantation.assignment.mandor-unassigned";
    public static final String SUPIR_ASSIGNED_KEY    = "plantation.assignment.supir-assigned";
    public static final String SUPIR_UNASSIGNED_KEY  = "plantation.assignment.supir-unassigned";

    @Bean
    public TopicExchange plantationExchange() {
        return new TopicExchange(PLANTATION_EXCHANGE, true, false);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
