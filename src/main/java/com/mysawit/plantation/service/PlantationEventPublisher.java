package com.mysawit.plantation.service;

import com.mysawit.plantation.client.IdentityServiceClient;
import com.mysawit.plantation.config.RabbitMQConfig;
import com.mysawit.plantation.event.PlantationAssignmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Publishes plantation domain events to RabbitMQ so other services
 * (harvest, shipment) can react without direct coupling.
 *
 * Event format matches PlantationAssignmentEvent in shipment-service:
 * { eventId, userId (UUID), name, role, plantationId, action, occurredAt }
 */
@Service
public class PlantationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PlantationEventPublisher.class);

    private static final String ACTION_ASSIGNED   = "ASSIGNED";
    private static final String ACTION_UNASSIGNED = "UNASSIGNED";
    private static final String ROLE_MANDOR = "MANDOR";
    private static final String ROLE_SUPIR  = "SUPIR";

    private final RabbitTemplate rabbitTemplate;
    private final IdentityServiceClient identityServiceClient;

    public PlantationEventPublisher(RabbitTemplate rabbitTemplate,
                                    IdentityServiceClient identityServiceClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.identityServiceClient = identityServiceClient;
    }

    public void publishMandorAssigned(Long plantationId, String mandorId) {
        String name = identityServiceClient.getUserName(mandorId);
        publish(RabbitMQConfig.MANDOR_ASSIGNED_KEY,
                buildEvent(mandorId, ROLE_MANDOR, toPlantationId(plantationId), ACTION_ASSIGNED, name));
    }

    public void publishMandorUnassigned(Long plantationId, String mandorId) {
        publish(RabbitMQConfig.MANDOR_UNASSIGNED_KEY,
                buildEvent(mandorId, ROLE_MANDOR, toPlantationId(plantationId), ACTION_UNASSIGNED, null));
    }

    public void publishSupirAssigned(Long plantationId, String supirId) {
        String name = identityServiceClient.getUserName(supirId);
        publish(RabbitMQConfig.SUPIR_ASSIGNED_KEY,
                buildEvent(supirId, ROLE_SUPIR, toPlantationId(plantationId), ACTION_ASSIGNED, name));
    }

    public void publishSupirUnassigned(Long plantationId, String supirId) {
        publish(RabbitMQConfig.SUPIR_UNASSIGNED_KEY,
                buildEvent(supirId, ROLE_SUPIR, toPlantationId(plantationId), ACTION_UNASSIGNED, null));
    }

    private String toPlantationId(Long id) {
        return String.valueOf(id);
    }

    private PlantationAssignmentEvent buildEvent(String userId, String role, String plantationId,
                                                 String action, String name) {
        return new PlantationAssignmentEvent(
                UUID.randomUUID().toString(),
                userId,
                name,
                role,
                plantationId,
                action,
                OffsetDateTime.now()
        );
    }

    private void publish(String routingKey, Object payload) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.PLANTATION_EXCHANGE, routingKey, payload);
            log.info("Published plantation event: routingKey={}", routingKey);
        } catch (Exception e) {
            log.error("Failed to publish plantation event: routingKey={}, error={}", routingKey, e.getMessage());
        }
    }
}
