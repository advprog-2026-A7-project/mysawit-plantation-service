package com.mysawit.plantation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import jakarta.persistence.ElementCollection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "plantations")
@EntityListeners(AuditingEntityListener.class)
public class Plantation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Double area;

    @Column(length = 1000)
    private String description;

    @Column(name = "owner_id")
    private String ownerId;

    @Column(name = "plant_date")
    private LocalDateTime plantDate;

    @Column(name = "mandor_id")
    private String mandorId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ElementCollection
    private List<Coordinate> coordinates = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "plantation_supir_ids", joinColumns = @JoinColumn(name = "plantation_id"))
    @Column(name = "supir_id")
    private Set<String> supirIds = new HashSet<>();

    // Constructors
    public Plantation() {
    }

    public Plantation(String code, String name, String location, Double area) {
        this.code = code;
        this.name = name;
        this.location = location;
        this.area = area;
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

    public String getMandorId() {
        return mandorId;
    }

    public void setMandorId(String mandorId) {
        this.mandorId = mandorId;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public Set<String> getSupirIds() {
        return supirIds;
    }

    public void setSupirIds(Set<String> supirIds) {
        this.supirIds = supirIds;
    }

    public void addSupir(String supirId) {
        this.supirIds.add(supirId);
    }

    public void removeSupir(String supirId) {
        this.supirIds.remove(supirId);
    }
}
