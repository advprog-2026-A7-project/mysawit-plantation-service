package com.mysawit.plantation.dto;

public class PlantationRequest extends BasePlantationRequest {

    private String ownerId;

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
