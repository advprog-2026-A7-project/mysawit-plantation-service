package com.mysawit.plantation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TransferMandorRequest {

    @NotBlank(message = "Mandor ID is required")
    private String mandorId;

    @NotNull(message = "From Plantation ID is required")
    private Long fromPlantationId;

    @NotNull(message = "To Plantation ID is required")
    private Long toPlantationId;

    public TransferMandorRequest() {}

    public String getMandorId() {
        return mandorId;
    }

    public void setMandorId(String mandorId) {
        this.mandorId = mandorId;
    }

    public Long getFromPlantationId() {
        return fromPlantationId;
    }

    public void setFromPlantationId(Long fromPlantationId) {
        this.fromPlantationId = fromPlantationId;
    }

    public Long getToPlantationId() {
        return toPlantationId;
    }

    public void setToPlantationId(Long toPlantationId) {
        this.toPlantationId = toPlantationId;
    }
}
