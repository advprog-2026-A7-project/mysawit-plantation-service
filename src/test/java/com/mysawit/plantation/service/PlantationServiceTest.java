package com.mysawit.plantation.service;

import com.mysawit.plantation.dto.PlantationRequest;
import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.repository.PlantationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlantationServiceTest {

    private PlantationRepository plantationRepository;
    private PlantationService plantationService;

    @BeforeEach
    void setUp() {
        plantationRepository = mock(PlantationRepository.class);
        plantationService = new PlantationService(plantationRepository);
    }

    @Test
    void getAllPlantationsReturnsRepositoryData() {
        when(plantationRepository.findAll()).thenReturn(List.of(new Plantation(), new Plantation()));

        List<Plantation> result = plantationService.getAllPlantations();

        assertEquals(2, result.size());
    }

    @Test
    void getPlantationByIdReturnsEntity() {
        Plantation plantation = new Plantation();
        when(plantationRepository.findById(1L)).thenReturn(Optional.of(plantation));

        Plantation result = plantationService.getPlantationById(1L);

        assertSame(plantation, result);
    }

    @Test
    void getPlantationByIdThrowsWhenMissing() {
        when(plantationRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> plantationService.getPlantationById(1L));

        assertEquals("Plantation not found with id: 1", exception.getMessage());
    }

    @Test
    void getPlantationsByOwnerIdReturnsRepositoryData() {
        when(plantationRepository.findByOwnerId(10L)).thenReturn(List.of(new Plantation()));

        assertEquals(1, plantationService.getPlantationsByOwnerId(10L).size());
    }

    @Test
    void createPlantationMapsAndSavesRequest() {
        PlantationRequest request = sampleRequest();
        when(plantationRepository.save(any(Plantation.class))).thenAnswer(inv -> inv.getArgument(0));

        Plantation result = plantationService.createPlantation(request);

        assertEquals("Plantation", result.getName());
        assertEquals("Riau", result.getLocation());
        assertEquals(10.0, result.getArea());
        assertEquals(10L, result.getOwnerId());
        assertEquals("desc", result.getDescription());
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.getPlantDate());
    }

    @Test
    void updatePlantationMapsAndSavesRequest() {
        Plantation existing = new Plantation();
        existing.setId(5L);
        when(plantationRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(plantationRepository.save(any(Plantation.class))).thenAnswer(inv -> inv.getArgument(0));

        Plantation result = plantationService.updatePlantation(5L, sampleRequest());

        assertEquals("Plantation", result.getName());
        assertEquals("Riau", result.getLocation());
        assertEquals(10.0, result.getArea());
    }

    @Test
    void updatePlantationThrowsWhenMissing() {
        when(plantationRepository.findById(5L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> plantationService.updatePlantation(5L, sampleRequest()));

        assertEquals("Plantation not found with id: 5", exception.getMessage());
    }

    @Test
    void deletePlantationDeletesEntityWhenFound() {
        Plantation existing = new Plantation();
        when(plantationRepository.findById(5L)).thenReturn(Optional.of(existing));

        plantationService.deletePlantation(5L);

        ArgumentCaptor<Plantation> captor = ArgumentCaptor.forClass(Plantation.class);
        verify(plantationRepository).delete(captor.capture());
        assertSame(existing, captor.getValue());
    }

    @Test
    void deletePlantationThrowsWhenMissing() {
        when(plantationRepository.findById(5L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> plantationService.deletePlantation(5L));

        assertEquals("Plantation not found with id: 5", exception.getMessage());
        verify(plantationRepository, never()).delete(any());
    }

    private PlantationRequest sampleRequest() {
        PlantationRequest request = new PlantationRequest();
        request.setName("Plantation");
        request.setLocation("Riau");
        request.setArea(10.0);
        request.setOwnerId(10L);
        request.setDescription("desc");
        request.setPlantDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        return request;
    }
}
