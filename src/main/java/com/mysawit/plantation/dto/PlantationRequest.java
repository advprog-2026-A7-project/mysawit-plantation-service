package com.mysawit.plantation.dto;

public class PlantationRequest extends BasePlantationRequest {

    private String code;

    private String ownerId;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
