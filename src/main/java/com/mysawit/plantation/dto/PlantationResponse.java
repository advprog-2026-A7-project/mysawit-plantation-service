package com.mysawit.plantation.dto;

import com.mysawit.plantation.model.Plantation;
import java.time.LocalDateTime;

public record PlantationResponse(
        Long id,
        String code,
        String name,
        String location,
        Double area,
        String description,
        String ownerId,
        String mandorId,
        LocalDateTime plantDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public PlantationResponse(Plantation plantation) {
        this(
                plantation.getId(),
                plantation.getCode(),
                plantation.getName(),
                plantation.getLocation(),
                plantation.getArea(),
                plantation.getDescription(),
                plantation.getOwnerId(),
                plantation.getMandorId(),
                plantation.getPlantDate(),
                plantation.getCreatedAt(),
                plantation.getUpdatedAt());
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public Double getArea() {
        return area;
    }

    public String getDescription() {
        return description;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getMandorId() {
        return mandorId;
    }

    public LocalDateTime getPlantDate() {
        return plantDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
