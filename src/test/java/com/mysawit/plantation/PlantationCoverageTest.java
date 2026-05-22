package com.mysawit.plantation;

import com.mysawit.plantation.controller.PlantationController;
import com.mysawit.plantation.dto.PlantationRequest;
import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.service.PlantationService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlantationCoverageTest {

    @Test
    void requestAndEntityAccessorsRoundTripValues() {
        LocalDateTime plantedAt = LocalDateTime.of(2026, 5, 22, 8, 0);
        PlantationRequest request = new PlantationRequest();
        request.setName("Block A");
        request.setLocation("Riau");
        request.setArea(12.5);
        request.setOwnerId(42L);
        request.setDescription("Prime block");
        request.setPlantDate(plantedAt);

        assertEquals("Block A", request.getName());
        assertEquals("Riau", request.getLocation());
        assertEquals(12.5, request.getArea());
        assertEquals(42L, request.getOwnerId());
        assertEquals("Prime block", request.getDescription());
        assertEquals(plantedAt, request.getPlantDate());

        Plantation plantation = new Plantation();
        plantation.setId(1L);
        plantation.setName(request.getName());
        plantation.setLocation(request.getLocation());
        plantation.setArea(request.getArea());
        plantation.setOwnerId(request.getOwnerId());
        plantation.setDescription(request.getDescription());
        plantation.setPlantDate(request.getPlantDate());
        plantation.setCreatedAt(plantedAt);
        plantation.setUpdatedAt(plantedAt.plusHours(1));

        assertEquals(1L, plantation.getId());
        assertEquals("Block A", plantation.getName());
        assertEquals("Riau", plantation.getLocation());
        assertEquals(12.5, plantation.getArea());
        assertEquals(42L, plantation.getOwnerId());
        assertEquals("Prime block", plantation.getDescription());
        assertEquals(plantedAt, plantation.getPlantDate());
        assertEquals(plantedAt, plantation.getCreatedAt());
        assertEquals(plantedAt.plusHours(1), plantation.getUpdatedAt());
    }

    @Test
    void createEndpointMapsServiceRuntimeExceptionToBadRequest() {
        PlantationService plantationService = mock(PlantationService.class);
        PlantationRequest request = new PlantationRequest();
        when(plantationService.createPlantation(request)).thenThrow(new RuntimeException("invalid plantation"));

        PlantationController controller = new PlantationController(plantationService);
        ResponseEntity<?> response = controller.createPlantation(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
