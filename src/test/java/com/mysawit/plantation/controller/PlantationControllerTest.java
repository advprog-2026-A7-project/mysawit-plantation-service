package com.mysawit.plantation.controller;

import com.mysawit.plantation.dto.PlantationRequest;
import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.service.PlantationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlantationControllerTest {

    private PlantationService plantationService;
    private PlantationController plantationController;

    @BeforeEach
    void setUp() {
        plantationService = mock(PlantationService.class);
        plantationController = new PlantationController(plantationService);
    }

    @Test
    void getAllPlantationsUsesOwnerFilter() {
        Plantation plantation = samplePlantation(1L);
        when(plantationService.getPlantationsByOwnerId(10L)).thenReturn(List.of(plantation));

        ResponseEntity<List<Plantation>> response = plantationController.getAllPlantations(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(plantationService).getPlantationsByOwnerId(10L);
        verify(plantationService, never()).getAllPlantations();
    }

    @Test
    void getAllPlantationsReturnsAllWhenNoFilter() {
        when(plantationService.getAllPlantations()).thenReturn(List.of(samplePlantation(1L), samplePlantation(2L)));

        ResponseEntity<List<Plantation>> response = plantationController.getAllPlantations(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(plantationService).getAllPlantations();
    }

    @Test
    void getPlantationByIdReturnsPlantation() {
        Plantation plantation = samplePlantation(1L);
        when(plantationService.getPlantationById(1L)).thenReturn(plantation);

        ResponseEntity<?> response = plantationController.getPlantationById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(plantation, response.getBody());
    }

    @Test
    void getPlantationByIdReturnsNotFoundWhenMissing() {
        when(plantationService.getPlantationById(1L)).thenThrow(new RuntimeException("missing"));

        ResponseEntity<?> response = plantationController.getPlantationById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("missing", ((Map<?, ?>) response.getBody()).get("error"));
    }

    @Test
    void createPlantationReturnsCreated() {
        Plantation plantation = samplePlantation(1L);
        PlantationRequest request = sampleRequest();
        when(plantationService.createPlantation(request)).thenReturn(plantation);

        ResponseEntity<?> response = plantationController.createPlantation(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(plantation, response.getBody());
    }

    @Test
    void createPlantationReturnsBadRequestOnError() {
        PlantationRequest request = sampleRequest();
        when(plantationService.createPlantation(request)).thenThrow(new RuntimeException("invalid"));

        ResponseEntity<?> response = plantationController.createPlantation(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("invalid", ((Map<?, ?>) response.getBody()).get("error"));
    }

    @Test
    void updatePlantationReturnsUpdated() {
        Plantation plantation = samplePlantation(1L);
        PlantationRequest request = sampleRequest();
        when(plantationService.updatePlantation(1L, request)).thenReturn(plantation);

        ResponseEntity<?> response = plantationController.updatePlantation(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(plantation, response.getBody());
    }

    @Test
    void updatePlantationReturnsNotFoundOnError() {
        PlantationRequest request = sampleRequest();
        when(plantationService.updatePlantation(1L, request)).thenThrow(new RuntimeException("missing"));

        ResponseEntity<?> response = plantationController.updatePlantation(1L, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("missing", ((Map<?, ?>) response.getBody()).get("error"));
    }

    @Test
    void deletePlantationReturnsSuccessMessage() {
        ResponseEntity<?> response = plantationController.deletePlantation(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Plantation deleted successfully", ((Map<?, ?>) response.getBody()).get("message"));
        verify(plantationService).deletePlantation(1L);
    }

    @Test
    void deletePlantationReturnsNotFoundOnError() {
        doThrow(new RuntimeException("missing")).when(plantationService).deletePlantation(1L);

        ResponseEntity<?> response = plantationController.deletePlantation(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("missing", ((Map<?, ?>) response.getBody()).get("error"));
    }

    @Test
    void healthReturnsUpStatus() {
        ResponseEntity<Map<String, String>> response = plantationController.health();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("mysawit-plantation-service", response.getBody().get("service"));
    }

    private Plantation samplePlantation(Long id) {
        Plantation plantation = new Plantation();
        plantation.setId(id);
        plantation.setName("Plantation");
        plantation.setLocation("Riau");
        plantation.setArea(10.0);
        plantation.setOwnerId(10L);
        plantation.setDescription("desc");
        plantation.setPlantDate(LocalDateTime.now());
        return plantation;
    }

    private PlantationRequest sampleRequest() {
        PlantationRequest request = new PlantationRequest();
        request.setName("Plantation");
        request.setLocation("Riau");
        request.setArea(10.0);
        request.setOwnerId(10L);
        request.setDescription("desc");
        request.setPlantDate(LocalDateTime.now());
        return request;
    }
}
