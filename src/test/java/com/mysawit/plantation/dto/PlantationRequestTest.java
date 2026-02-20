package com.mysawit.plantation.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PlantationRequestTest {

    @Test
    void gettersAndSettersWork() {
        PlantationRequest request = new PlantationRequest();
        LocalDateTime plantDate = LocalDateTime.of(2026, 2, 1, 9, 30);

        request.setName("Plantation");
        request.setLocation("Riau");
        request.setArea(55.5);
        request.setOwnerId(1L);
        request.setDescription("desc");
        request.setPlantDate(plantDate);

        assertEquals("Plantation", request.getName());
        assertEquals("Riau", request.getLocation());
        assertEquals(55.5, request.getArea());
        assertEquals(1L, request.getOwnerId());
        assertEquals("desc", request.getDescription());
        assertEquals(plantDate, request.getPlantDate());
    }
}
