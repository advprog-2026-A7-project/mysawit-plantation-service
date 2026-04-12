package com.mysawit.plantation.dto;

import jakarta.validation.constraints.NotBlank;

public class AssignMandorRequest {

    @NotBlank(message = "Mandor ID is required")
    private String mandorId;

    public AssignMandorRequest() {}

    public String getMandorId() {
        return mandorId;
    }

    public void setMandorId(String mandorId) {
        this.mandorId = mandorId;
    }
}
