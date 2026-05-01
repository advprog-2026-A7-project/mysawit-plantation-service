package com.mysawit.plantation.dto;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreatePlantationRequestTest {

    @Test
    void gettersAndSettersWork() {
        CreatePlantationRequest request = new CreatePlantationRequest();
        LocalDateTime plantDate = LocalDateTime.of(2026, 2, 1, 9, 30);

        request.setName("Plantation");
        request.setLocation("Riau");
        request.setArea(55.5);
        request.setOwnerId("owner-1");
        request.setDescription("desc");
        request.setPlantDate(plantDate);

        assertEquals("Plantation", request.getName());
        assertEquals("Riau", request.getLocation());
        assertEquals(55.5, request.getArea());
        assertEquals("owner-1", request.getOwnerId());
        assertEquals("desc", request.getDescription());
        assertEquals(plantDate, request.getPlantDate());
    }
}
