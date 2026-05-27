package com.mysawit.plantation.event;

import java.time.OffsetDateTime;

public class PlantationAssignmentEvent {
    private String eventId;
    private String userId;
    private String name;
    private String role;
    private String plantationId;
    private String action;
    private OffsetDateTime occurredAt;

    public PlantationAssignmentEvent() {}

    public PlantationAssignmentEvent(
            String eventId,
            String userId,
            String name,
            String role,
            String plantationId,
            String action,
            OffsetDateTime occurredAt
    ) {
        this.eventId = eventId;
        this.userId = userId;
        this.name = name;
        this.role = role;
        this.plantationId = plantationId;
        this.action = action;
        this.occurredAt = occurredAt;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPlantationId() {
        return plantationId;
    }

    public void setPlantationId(String plantationId) {
        this.plantationId = plantationId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}
