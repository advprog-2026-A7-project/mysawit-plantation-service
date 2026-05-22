package com.mysawit.plantation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TransferSupirRequest {

    @NotBlank(message = "Supir ID is required")
    private String supirId;

    @NotNull(message = "From Plantation ID is required")
    private Long fromPlantationId;

    @NotNull(message = "To Plantation ID is required")
    private Long toPlantationId;

    public String getSupirId() {
        return supirId;
    }

    public void setSupirId(String supirId) {
        this.supirId = supirId;
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
