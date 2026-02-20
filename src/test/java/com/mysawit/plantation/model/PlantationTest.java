package com.mysawit.plantation.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PlantationTest {

    @Test
    void gettersAndSettersWork() {
        Plantation plantation = new Plantation();
        LocalDateTime plantDate = LocalDateTime.of(2026, 3, 1, 8, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 1, 9, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 3, 1, 10, 0);

        plantation.setId(1L);
        plantation.setName("Plantation");
        plantation.setLocation("Riau");
        plantation.setArea(100.0);
        plantation.setOwnerId(2L);
        plantation.setDescription("desc");
        plantation.setPlantDate(plantDate);
        plantation.setCreatedAt(createdAt);
        plantation.setUpdatedAt(updatedAt);

        assertEquals(1L, plantation.getId());
        assertEquals("Plantation", plantation.getName());
        assertEquals("Riau", plantation.getLocation());
        assertEquals(100.0, plantation.getArea());
        assertEquals(2L, plantation.getOwnerId());
        assertEquals("desc", plantation.getDescription());
        assertEquals(plantDate, plantation.getPlantDate());
        assertEquals(createdAt, plantation.getCreatedAt());
        assertEquals(updatedAt, plantation.getUpdatedAt());
    }

    @Test
    void lifecycleHooksSetTimestamps() {
        Plantation plantation = new Plantation();

        plantation.onCreate();

        assertNotNull(plantation.getCreatedAt());
        assertNotNull(plantation.getUpdatedAt());

        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
        plantation.setUpdatedAt(beforeUpdate.minusDays(1));

        plantation.onUpdate();

        assertTrue(plantation.getUpdatedAt().isAfter(beforeUpdate));
    }
}
