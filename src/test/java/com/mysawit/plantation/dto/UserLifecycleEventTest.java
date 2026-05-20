package com.mysawit.plantation.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserLifecycleEventTest {

    @Test
    void gettersAndSettersWork() {
        Instant occurredAt = Instant.parse("2026-05-21T00:00:00Z");
        UserLifecycleEvent event = new UserLifecycleEvent();

        event.setUserId("user-1");
        event.setRole("SUPIR");
        event.setName("Budi Driver");
        event.setOccurredAt(occurredAt);

        assertEquals("user-1", event.getUserId());
        assertEquals("SUPIR", event.getRole());
        assertEquals("Budi Driver", event.getName());
        assertEquals(occurredAt, event.getOccurredAt());
    }
}
