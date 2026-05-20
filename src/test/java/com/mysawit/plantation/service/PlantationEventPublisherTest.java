package com.mysawit.plantation.service;

import com.mysawit.plantation.client.IdentityServiceClient;
import com.mysawit.plantation.config.RabbitMQConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlantationEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private IdentityServiceClient identityServiceClient;

    private PlantationEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new PlantationEventPublisher(rabbitTemplate, identityServiceClient);
    }

    @Test
    void publishSupirAssignedIncludesIdentityName() {
        when(identityServiceClient.getUserName("supir-1")).thenReturn("Driver One");

        publisher.publishSupirAssigned(2L, "supir-1");

        Map<String, Object> event = captureEvent(RabbitMQConfig.SUPIR_ASSIGNED_KEY);
        assertEquals("supir-1", event.get("userId"));
        assertEquals("SUPIR", event.get("role"));
        assertEquals("00000000-0000-0000-0000-000000000002", event.get("plantationId"));
        assertEquals("ASSIGNED", event.get("action"));
        assertEquals("Driver One", event.get("name"));
        assertNotNull(event.get("eventId"));
        assertNotNull(event.get("occurredAt"));
    }

    @Test
    void publishMandorUnassignedOmitsName() {
        publisher.publishMandorUnassigned(3L, "mandor-1");

        Map<String, Object> event = captureEvent(RabbitMQConfig.MANDOR_UNASSIGNED_KEY);
        assertEquals("mandor-1", event.get("userId"));
        assertEquals("MANDOR", event.get("role"));
        assertEquals("00000000-0000-0000-0000-000000000003", event.get("plantationId"));
        assertEquals("UNASSIGNED", event.get("action"));
        assertFalse(event.containsKey("name"));
    }

    @Test
    void publishSupirUnassignedUsesSupirRoutingKey() {
        publisher.publishSupirUnassigned(4L, "supir-1");

        Map<String, Object> event = captureEvent(RabbitMQConfig.SUPIR_UNASSIGNED_KEY);
        assertEquals("supir-1", event.get("userId"));
        assertEquals("SUPIR", event.get("role"));
        assertEquals("UNASSIGNED", event.get("action"));
    }

    @Test
    void publishMandorAssignedSwallowsRabbitFailure() {
        when(identityServiceClient.getUserName("mandor-1")).thenReturn("Mandor One");
        doThrow(new RuntimeException("broker down"))
                .when(rabbitTemplate)
                .convertAndSend(eq(RabbitMQConfig.PLANTATION_EXCHANGE), eq(RabbitMQConfig.MANDOR_ASSIGNED_KEY), anyMap());

        assertDoesNotThrow(() -> publisher.publishMandorAssigned(1L, "mandor-1"));
    }

    private Map<String, Object> captureEvent(String routingKey) {
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.PLANTATION_EXCHANGE),
                eq(routingKey),
                captor.capture()
        );
        return captor.getValue();
    }
}
