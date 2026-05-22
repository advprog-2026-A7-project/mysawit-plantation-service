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
    public static final String USER_EXCHANGE = "user.exchange";
    public static final String USER_UPDATED_QUEUE = "plantation.user.updated.queue";
    public static final String USER_DELETED_QUEUE = "plantation.user.deleted.queue";
    public static final String USER_UPDATED_KEY = "user.updated";
    public static final String USER_DELETED_KEY = "user.deleted";

    @Bean
    public TopicExchange plantationExchange() {
        return new TopicExchange(PLANTATION_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE, true, false);
    }

    @Bean
    public Queue plantationUserUpdatedQueue() {
        return new Queue(USER_UPDATED_QUEUE, true);
    }

    @Bean
    public Binding plantationUserUpdatedBinding(Queue plantationUserUpdatedQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(plantationUserUpdatedQueue).to(userExchange).with(USER_UPDATED_KEY);
    }

    @Bean
    public Queue plantationUserDeletedQueue() {
        return new Queue(USER_DELETED_QUEUE, true);
    }

    @Bean
    public Binding plantationUserDeletedBinding(Queue plantationUserDeletedQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(plantationUserDeletedQueue).to(userExchange).with(USER_DELETED_KEY);
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
