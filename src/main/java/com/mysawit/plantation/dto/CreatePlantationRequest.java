package com.mysawit.plantation.dto;

import jakarta.validation.constraints.Size;

public class CreatePlantationRequest extends BasePlantationRequest {

    @Size(max = 50, message = "Plantation code must not exceed 50 characters")
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
