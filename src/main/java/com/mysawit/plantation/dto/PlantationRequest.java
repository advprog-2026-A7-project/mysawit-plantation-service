package com.mysawit.plantation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public class PlantationRequest {
    
    @NotBlank(message = "Plantation name is required")
    private String name;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    @NotNull(message = "Area is required")
    @Positive(message = "Area must be positive")
    private Double area;
    
    private Long ownerId;
    private String description;
    private LocalDateTime plantDate;
    
    // Getters and Setters
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
    
    public Long getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getPlantDate() {
        return plantDate;
    }
    
    public void setPlantDate(LocalDateTime plantDate) {
        this.plantDate = plantDate;
    }
}
