package com.mysawit.plantation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import com.mysawit.plantation.model.Coordinate;

public class PlantationRequest {
    
    @NotBlank(message = "Plantation name is required")
    private String name;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    @NotNull(message = "Area is required")
    @Positive(message = "Area must be positive")
    private Double area;
    private String ownerId;
    private String description;
    private LocalDateTime plantDate;

    @NotNull(message = "Coordinates are required")
    @Size(min = 4, max = 4, message = "Exactly 4 coordinates are required")
    @Valid
    private List<Coordinate> coordinates;
    
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
    
    public String getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(String ownerId) {
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

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }
    
    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }
}
