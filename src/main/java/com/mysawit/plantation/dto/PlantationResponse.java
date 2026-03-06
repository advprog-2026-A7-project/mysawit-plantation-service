package com.mysawit.plantation.dto;

import com.mysawit.plantation.model.Plantation;
import java.time.LocalDateTime;

public class PlantationResponse {
    
    private Long id;
    private String code;
    private String name;
    private String location;
    private Double area;
    private String description;
    private String ownerId;
    private LocalDateTime plantDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public PlantationResponse() {
    }

    // Constructor with entity mapping
    public PlantationResponse(Plantation plantation) {
        this.id = plantation.getId();
        this.code = plantation.getCode();
        this.name = plantation.getName();
        this.location = plantation.getLocation();
        this.area = plantation.getArea();
        this.description = plantation.getDescription();
        this.ownerId = plantation.getOwnerId();
        this.plantDate = plantation.getPlantDate();
        this.createdAt = plantation.getCreatedAt();
        this.updatedAt = plantation.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public LocalDateTime getPlantDate() {
        return plantDate;
    }

    public void setPlantDate(LocalDateTime plantDate) {
        this.plantDate = plantDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
