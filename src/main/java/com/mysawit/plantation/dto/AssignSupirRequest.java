package com.mysawit.plantation.dto;

import jakarta.validation.constraints.NotBlank;

public class AssignSupirRequest {

    @NotBlank(message = "Supir ID is required")
    private String supirId;

    public String getSupirId() {
        return supirId;
    }

    public void setSupirId(String supirId) {
        this.supirId = supirId;
    }
}
