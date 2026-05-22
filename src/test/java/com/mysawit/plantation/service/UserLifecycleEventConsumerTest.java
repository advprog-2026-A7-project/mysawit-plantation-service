package com.mysawit.plantation.service;

import com.mysawit.plantation.dto.UserLifecycleEvent;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class UserLifecycleEventConsumerTest {

    @Test
    void onUserUpdatedDelegatesToPlantationService() {
        PlantationService plantationService = mock(PlantationService.class);
        UserLifecycleEventConsumer consumer = new UserLifecycleEventConsumer(plantationService);
        UserLifecycleEvent event = new UserLifecycleEvent();
        event.setUserId("user-1");
        event.setRole("SUPIR");

        consumer.onUserUpdated(event);

        verify(plantationService).syncAssignmentForUpdatedUser("user-1", "SUPIR");
    }

    @Test
    void onUserDeletedDelegatesToPlantationService() {
        PlantationService plantationService = mock(PlantationService.class);
        UserLifecycleEventConsumer consumer = new UserLifecycleEventConsumer(plantationService);
        UserLifecycleEvent event = new UserLifecycleEvent();
        event.setUserId("user-1");

        consumer.onUserDeleted(event);

        verify(plantationService).removeAssignmentsForDeletedUser("user-1");
    }

    @Test
    void nullEventsAreIgnored() {
        PlantationService plantationService = mock(PlantationService.class);
        UserLifecycleEventConsumer consumer = new UserLifecycleEventConsumer(plantationService);

        consumer.onUserUpdated(null);
        consumer.onUserDeleted(null);

        verify(plantationService, never()).syncAssignmentForUpdatedUser(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(plantationService, never()).removeAssignmentsForDeletedUser(org.mockito.ArgumentMatchers.any());
    }
}
