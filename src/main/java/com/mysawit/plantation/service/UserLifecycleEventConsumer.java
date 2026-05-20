package com.mysawit.plantation.service;

import com.mysawit.plantation.config.RabbitMQConfig;
import com.mysawit.plantation.dto.UserLifecycleEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UserLifecycleEventConsumer {

    private final PlantationService plantationService;

    public UserLifecycleEventConsumer(PlantationService plantationService) {
        this.plantationService = plantationService;
    }

    @RabbitListener(queues = RabbitMQConfig.USER_UPDATED_QUEUE)
    public void onUserUpdated(UserLifecycleEvent event) {
        if (event != null) {
            plantationService.syncAssignmentForUpdatedUser(event.getUserId(), event.getRole());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.USER_DELETED_QUEUE)
    public void onUserDeleted(UserLifecycleEvent event) {
        if (event != null) {
            plantationService.removeAssignmentsForDeletedUser(event.getUserId());
        }
    }
}
