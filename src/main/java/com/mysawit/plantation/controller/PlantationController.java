package com.mysawit.plantation.controller;

import com.mysawit.plantation.dto.PlantationRequest;
import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.service.PlantationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plantations")
public class PlantationController {
    
    private final PlantationService plantationService;
    
    public PlantationController(PlantationService plantationService) {
        this.plantationService = plantationService;
    }
    
    @GetMapping
    public ResponseEntity<List<Plantation>> getAllPlantations(
            @RequestParam(required = false) Long ownerId) {
        List<Plantation> plantations;
        if (ownerId != null) {
            plantations = plantationService.getPlantationsByOwnerId(ownerId);
        } else {
            plantations = plantationService.getAllPlantations();
        }
        return ResponseEntity.ok(plantations);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getPlantationById(@PathVariable Long id) {
        try {
            Plantation plantation = plantationService.getPlantationById(id);
            return ResponseEntity.ok(plantation);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createPlantation(@Valid @RequestBody PlantationRequest request) {
        try {
            Plantation plantation = plantationService.createPlantation(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(plantation);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlantation(
            @PathVariable Long id,
            @Valid @RequestBody PlantationRequest request) {
        try {
            Plantation plantation = plantationService.updatePlantation(id, request);
            return ResponseEntity.ok(plantation);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlantation(@PathVariable Long id) {
        try {
            plantationService.deletePlantation(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Plantation deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "mysawit-plantation-service");
        return ResponseEntity.ok(health);
    }
}
