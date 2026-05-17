package com.mysawit.plantation.dto;

import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.model.Coordinate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record PlantationResponse(
        Long id,
        String code,
        String name,
        String location,
        Double area,
        String description,
        String ownerId,
        String mandorId,
        Set<String> supirIds,
        LocalDateTime plantDate,
        List<Coordinate> coordinates,
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
                plantation.getSupirIds(),
                plantation.getPlantDate(),
                plantation.getCoordinates(),
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

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }
}
