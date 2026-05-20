package com.mysawit.plantation.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    void declaresPlantationAndUserExchanges() {
        TopicExchange plantationExchange = config.plantationExchange();
        TopicExchange userExchange = config.userExchange();

        assertEquals(RabbitMQConfig.PLANTATION_EXCHANGE, plantationExchange.getName());
        assertEquals(RabbitMQConfig.USER_EXCHANGE, userExchange.getName());
    }

    @Test
    void declaresUserLifecycleQueuesAndBindings() {
        TopicExchange userExchange = config.userExchange();
        Queue updatedQueue = config.plantationUserUpdatedQueue();
        Queue deletedQueue = config.plantationUserDeletedQueue();

        Binding updatedBinding = config.plantationUserUpdatedBinding(updatedQueue, userExchange);
        Binding deletedBinding = config.plantationUserDeletedBinding(deletedQueue, userExchange);

        assertEquals(RabbitMQConfig.USER_UPDATED_QUEUE, updatedQueue.getName());
        assertEquals(RabbitMQConfig.USER_DELETED_QUEUE, deletedQueue.getName());
        assertEquals(RabbitMQConfig.USER_UPDATED_KEY, updatedBinding.getRoutingKey());
        assertEquals(RabbitMQConfig.USER_DELETED_KEY, deletedBinding.getRoutingKey());
    }

    @Test
    void configuresJsonRabbitTemplate() {
        RabbitTemplate template = config.rabbitTemplate(mock(ConnectionFactory.class));

        assertInstanceOf(Jackson2JsonMessageConverter.class, config.messageConverter());
        assertInstanceOf(Jackson2JsonMessageConverter.class, template.getMessageConverter());
    }
}
