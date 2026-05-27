package com.mysawit.plantation.event;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlantationAssignmentEventTest {

    @Test
    void constructorStoresAllFields() {
        OffsetDateTime occurredAt = OffsetDateTime.parse("2026-05-25T10:00:00+07:00");

        PlantationAssignmentEvent event = new PlantationAssignmentEvent(
                "event-1",
                "user-1",
                "User One",
                "MANDOR",
                "plantation-1",
                "ASSIGNED",
                occurredAt
        );

        assertEquals("event-1", event.getEventId());
        assertEquals("user-1", event.getUserId());
        assertEquals("User One", event.getName());
        assertEquals("MANDOR", event.getRole());
        assertEquals("plantation-1", event.getPlantationId());
        assertEquals("ASSIGNED", event.getAction());
        assertEquals(occurredAt, event.getOccurredAt());
    }

    @Test
    void settersStoreAllFields() {
        PlantationAssignmentEvent event = new PlantationAssignmentEvent();
        assertNull(event.getEventId());

        OffsetDateTime occurredAt = OffsetDateTime.parse("2026-05-25T11:00:00+07:00");
        event.setEventId("event-2");
        event.setUserId("user-2");
        event.setName("User Two");
        event.setRole("SUPIR");
        event.setPlantationId("plantation-2");
        event.setAction("UNASSIGNED");
        event.setOccurredAt(occurredAt);

        assertEquals("event-2", event.getEventId());
        assertEquals("user-2", event.getUserId());
        assertEquals("User Two", event.getName());
        assertEquals("SUPIR", event.getRole());
        assertEquals("plantation-2", event.getPlantationId());
        assertEquals("UNASSIGNED", event.getAction());
        assertEquals(occurredAt, event.getOccurredAt());
    }
}
