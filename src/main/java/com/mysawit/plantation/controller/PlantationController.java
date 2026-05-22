package com.mysawit.plantation.controller;

import com.mysawit.plantation.dto.AssignMandorRequest;
import com.mysawit.plantation.dto.AssignSupirRequest;
import com.mysawit.plantation.dto.CreatePlantationRequest;
import com.mysawit.plantation.dto.PlantationResponse;
import com.mysawit.plantation.dto.SupirResponse;
import com.mysawit.plantation.dto.TransferMandorRequest;
import com.mysawit.plantation.dto.TransferSupirRequest;
import com.mysawit.plantation.dto.UpdatePlantationRequest;
import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.service.PlantationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/plantations")
public class PlantationController {

    private final PlantationService plantationService;

    public PlantationController(PlantationService plantationService) {
        this.plantationService = plantationService;
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<PlantationResponse>> getPlantations(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code) {
        List<PlantationResponse> responses = plantationService.searchPlantations(name, code).stream()
                .map(PlantationResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<PlantationResponse> getPlantationById(@PathVariable Long id) {
        Plantation plantation = plantationService.getPlantationById(id);
        return ResponseEntity.ok(new PlantationResponse(plantation));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<PlantationResponse>> getPlantationsByOwnerId(@PathVariable String ownerId) {
        List<PlantationResponse> responses = plantationService.getPlantationsByOwner(ownerId).stream()
                .map(PlantationResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PlantationResponse> createPlantation(
            @Valid @RequestBody CreatePlantationRequest request) {
        Plantation plantation = plantationService.createPlantation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PlantationResponse(plantation));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PlantationResponse> updatePlantation(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePlantationRequest request) {
        Plantation plantation = plantationService.updatePlantation(id, request);
        return ResponseEntity.ok(new PlantationResponse(plantation));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlantation(@PathVariable Long id) {
        plantationService.deletePlantation(id);
        return ResponseEntity.noContent().build();
    }

    // ── MANDOR MANAGEMENT ────────────────────────────────────────────────────

    /** Assign mandor to plantation (constraint: mandor hanya satu per kebun) */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/mandor")
    public ResponseEntity<PlantationResponse> assignMandor(
            @PathVariable Long id,
            @Valid @RequestBody AssignMandorRequest request) {
        Plantation plantation = plantationService.assignMandor(id, request.getMandorId());
        return ResponseEntity.ok(new PlantationResponse(plantation));
    }

    /**
     * Unassign mandor dari kebun.
     * Constraint docs: "ketika dicopot, Admin Utama harus segera menugaskan mandor ke kebun lainnya"
     * — enforcement ada di client/UI, bukan di backend (backend hanya support unassign).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/mandor")
    public ResponseEntity<PlantationResponse> unassignMandor(@PathVariable Long id) {
        Plantation plantation = plantationService.unassignMandor(id);
        return ResponseEntity.ok(new PlantationResponse(plantation));
    }

    /** Transfer mandor antar kebun atomik */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/transfer-mandor")
    public ResponseEntity<Void> transferMandor(
            @Valid @RequestBody TransferMandorRequest request) {
        plantationService.transferMandor(
                request.getMandorId(),
                request.getFromPlantationId(),
                request.getToPlantationId()
        );
        return ResponseEntity.ok().build();
    }

    // ── SUPIR TRUK MANAGEMENT ────────────────────────────────────────────────

    /** Lihat daftar supir yang ditugaskan pada sebuah kebun */
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANDOR')")
    @GetMapping("/{id}/supirs")
    public ResponseEntity<Set<String>> getSupirsByPlantation(@PathVariable Long id) {
        return ResponseEntity.ok(plantationService.getSupirsByPlantation(id));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANDOR')")
    @GetMapping("/{id}/supirs/details")
    public ResponseEntity<List<SupirResponse>> getSupirDetailsByPlantation(
            @PathVariable Long id,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(plantationService.getSupirDetailsByPlantation(id, name));
    }

    /**
     * Assign supir truk ke kebun.
     * Constraint docs: supir harus ditempatkan di kebun sebelum bisa melakukan aksi.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/supirs")
    public ResponseEntity<PlantationResponse> assignSupir(
            @PathVariable Long id,
            @Valid @RequestBody AssignSupirRequest request) {
        Plantation plantation = plantationService.assignSupir(id, request.getSupirId());
        return ResponseEntity.ok(new PlantationResponse(plantation));
    }

    /**
     * Unassign supir dari kebun.
     * Constraint docs: "ketika dicopot, Admin Utama harus segera menugaskan Supir Truk ke kebun lainnya"
     * — enforcement ada di UI.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/supirs/{supirId}")
    public ResponseEntity<PlantationResponse> unassignSupir(
            @PathVariable Long id,
            @PathVariable String supirId) {
        Plantation plantation = plantationService.unassignSupir(id, supirId);
        return ResponseEntity.ok(new PlantationResponse(plantation));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/transfer-supir")
    public ResponseEntity<Void> transferSupir(
            @Valid @RequestBody TransferSupirRequest request) {
        plantationService.transferSupir(
                request.getSupirId(),
                request.getFromPlantationId(),
                request.getToPlantationId()
        );
        return ResponseEntity.ok().build();
    }
}
