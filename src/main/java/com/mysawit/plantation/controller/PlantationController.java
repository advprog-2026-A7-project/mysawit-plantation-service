package com.mysawit.plantation.controller;

import com.mysawit.plantation.dto.PlantationRequest;
import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.service.PlantationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            return ResponseEntity.ok(plantationService.getPlantationById(id));
        } catch (RuntimeException e) {
            return notFoundError(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> createPlantation(@Valid @RequestBody PlantationRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(plantationService.createPlantation(request));
        } catch (RuntimeException e) {
            return badRequestError(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlantation(@PathVariable Long id, @Valid @RequestBody PlantationRequest request) {
        try {
            return ResponseEntity.ok(plantationService.updatePlantation(id, request));
        } catch (RuntimeException e) {
            return notFoundError(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlantation(@PathVariable Long id) {
        try {
            plantationService.deletePlantation(id);
            return ResponseEntity.ok(Map.of("message", "Plantation deleted successfully"));
        } catch (RuntimeException e) {
            return notFoundError(e);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "mysawit-plantation-service"));
    }

    private ResponseEntity<Map<String, String>> notFoundError(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    private ResponseEntity<Map<String, String>> badRequestError(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
