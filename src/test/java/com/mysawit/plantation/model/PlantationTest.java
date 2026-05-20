package com.mysawit.plantation.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
        plantation.setOwnerId("2");
        plantation.setDescription("desc");
        plantation.setPlantDate(plantDate);
        plantation.setCreatedAt(createdAt);
        plantation.setUpdatedAt(updatedAt);
        plantation.setCode("PLT-12345678");
        plantation.setMandorId("mandor-1");
        plantation.setCoordinates(List.of(new Coordinate(0.0, 0.0)));
        plantation.setSupirIds(new java.util.HashSet<>(Set.of("supir-1")));
        plantation.addSupir("supir-2");
        plantation.removeSupir("supir-1");

        assertEquals(1L, plantation.getId());
        assertEquals("PLT-12345678", plantation.getCode());
        assertEquals("Plantation", plantation.getName());
        assertEquals("Riau", plantation.getLocation());
        assertEquals(100.0, plantation.getArea());
        assertEquals("2", plantation.getOwnerId());
        assertEquals("desc", plantation.getDescription());
        assertEquals(plantDate, plantation.getPlantDate());
        assertEquals(createdAt, plantation.getCreatedAt());
        assertEquals(updatedAt, plantation.getUpdatedAt());
        assertEquals("mandor-1", plantation.getMandorId());
        assertEquals(List.of(new Coordinate(0.0, 0.0)), plantation.getCoordinates());
        assertEquals(Set.of("supir-2"), plantation.getSupirIds());
    }


}
