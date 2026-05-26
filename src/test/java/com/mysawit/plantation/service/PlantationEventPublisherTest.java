package com.mysawit.plantation.service;

import com.mysawit.plantation.client.IdentityServiceClient;
import com.mysawit.plantation.config.RabbitMQConfig;
import com.mysawit.plantation.event.PlantationAssignmentEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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

        PlantationAssignmentEvent event = captureEvent(RabbitMQConfig.SUPIR_ASSIGNED_KEY);
        assertEquals("supir-1", event.getUserId());
        assertEquals("SUPIR", event.getRole());
        assertEquals("2", event.getPlantationId());
        assertEquals("ASSIGNED", event.getAction());
        assertEquals("Driver One", event.getName());
        assertNotNull(event.getEventId());
        assertNotNull(event.getOccurredAt());
    }

    @Test
    void publishMandorUnassignedOmitsName() {
        publisher.publishMandorUnassigned(3L, "mandor-1");

        PlantationAssignmentEvent event = captureEvent(RabbitMQConfig.MANDOR_UNASSIGNED_KEY);
        assertEquals("mandor-1", event.getUserId());
        assertEquals("MANDOR", event.getRole());
        assertEquals("3", event.getPlantationId());
        assertEquals("UNASSIGNED", event.getAction());
        assertNull(event.getName());
    }

    @Test
    void publishSupirUnassignedUsesSupirRoutingKey() {
        publisher.publishSupirUnassigned(4L, "supir-1");

        PlantationAssignmentEvent event = captureEvent(RabbitMQConfig.SUPIR_UNASSIGNED_KEY);
        assertEquals("supir-1", event.getUserId());
        assertEquals("SUPIR", event.getRole());
        assertEquals("UNASSIGNED", event.getAction());
    }

    @Test
    void publishMandorAssignedSwallowsRabbitFailure() {
        when(identityServiceClient.getUserName("mandor-1")).thenReturn("Mandor One");
        doThrow(new RuntimeException("broker down"))
                .when(rabbitTemplate)
                .convertAndSend(eq(RabbitMQConfig.PLANTATION_EXCHANGE), eq(RabbitMQConfig.MANDOR_ASSIGNED_KEY), any(PlantationAssignmentEvent.class));

        assertDoesNotThrow(() -> publisher.publishMandorAssigned(1L, "mandor-1"));
    }

    private PlantationAssignmentEvent captureEvent(String routingKey) {
        ArgumentCaptor<PlantationAssignmentEvent> captor = ArgumentCaptor.forClass(PlantationAssignmentEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.PLANTATION_EXCHANGE),
                eq(routingKey),
                captor.capture()
        );
        return captor.getValue();
    }
}
