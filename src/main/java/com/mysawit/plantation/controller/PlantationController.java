package com.mysawit.plantation.controller;

import com.mysawit.plantation.dto.CreatePlantationRequest;
import com.mysawit.plantation.dto.PlantationResponse;
import com.mysawit.plantation.dto.UpdatePlantationRequest;
import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.service.PlantationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/plantations")
public class PlantationController {

    private final PlantationService plantationService;

    public PlantationController(PlantationService plantationService) {
        this.plantationService = plantationService;
    }

    @GetMapping
    public ResponseEntity<List<PlantationResponse>> getAllPlantations() {
        List<PlantationResponse> responses = plantationService.getAllPlantations().stream()
                .map(PlantationResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantationResponse> getPlantationById(@PathVariable Long id) {
        Plantation plantation = plantationService.getPlantationById(id);
        return ResponseEntity.ok(new PlantationResponse(plantation));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<PlantationResponse>> getPlantationsByOwnerId(@PathVariable String ownerId) {
        List<PlantationResponse> responses = plantationService.getPlantationsByOwner(ownerId).stream()
                .map(PlantationResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<PlantationResponse> createPlantation(
            @Valid @RequestBody CreatePlantationRequest request) {
        Plantation plantation = plantationService.createPlantation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PlantationResponse(plantation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlantationResponse> updatePlantation(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePlantationRequest request) {
        Plantation plantation = plantationService.updatePlantation(id, request);
        return ResponseEntity.ok(new PlantationResponse(plantation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlantation(@PathVariable Long id) {
        plantationService.deletePlantation(id);
        return ResponseEntity.noContent().build();
    }
}
