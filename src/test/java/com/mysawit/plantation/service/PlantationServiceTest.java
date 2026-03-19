package com.mysawit.plantation.service;

import com.mysawit.plantation.dto.CreatePlantationRequest;
import com.mysawit.plantation.dto.PlantationRequest;
import com.mysawit.plantation.dto.UpdatePlantationRequest;
import com.mysawit.plantation.exception.PlantationNotFoundException;
import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.repository.PlantationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlantationServiceTest {

    @Mock
    private PlantationRepository plantationRepository;

    @InjectMocks
    private PlantationService plantationService;

    @Test
    void getAllPlantationsReturnsList() {
        when(plantationRepository.findAll()).thenReturn(List.of(new Plantation(), new Plantation()));

        List<Plantation> result = plantationService.getAllPlantations();

        assertEquals(2, result.size());
    }

    @Test
    void getPlantationByIdSuccess() {
        Plantation plantation = new Plantation();
        when(plantationRepository.findById(1L)).thenReturn(Optional.of(plantation));

        Plantation result = plantationService.getPlantationById(1L);

        assertSame(plantation, result);
    }

    @Test
    void getPlantationByIdThrowsExceptionWhenMissing() {
        when(plantationRepository.findById(1L)).thenReturn(Optional.empty());

        PlantationNotFoundException exception = assertThrows(
                PlantationNotFoundException.class,
                () -> plantationService.getPlantationById(1L)
        );

        assertEquals("Plantation not found with id: 1", exception.getMessage());
    }

    @Test
    void getPlantationsByOwnerReturnsRepositoryData() {
        when(plantationRepository.findByOwnerId("10")).thenReturn(List.of(new Plantation()));

        assertEquals(1, plantationService.getPlantationsByOwner("10").size());
    }

    @Test
    void getPlantationsByOwnerIdDelegatesToPrimaryMethod() {
        when(plantationRepository.findByOwnerId("10")).thenReturn(List.of(new Plantation()));

        assertEquals(1, plantationService.getPlantationsByOwnerId("10").size());
    }

    @Test
    void createPlantationSavesEntityWithGeneratedCode() {
        CreatePlantationRequest request = sampleCreateRequest();
        when(plantationRepository.save(any(Plantation.class))).thenAnswer(inv -> inv.getArgument(0));

        Plantation result = plantationService.createPlantation(request);

        ArgumentCaptor<Plantation> captor = ArgumentCaptor.forClass(Plantation.class);
        verify(plantationRepository).save(captor.capture());
        Plantation saved = captor.getValue();

        assertNotNull(saved.getCode());
        assertTrue(saved.getCode().matches("PLT-[A-F0-9]{8}"));
        assertEquals("Plantation", saved.getName());
        assertEquals("Riau", saved.getLocation());
        assertEquals(10.0, saved.getArea());
        assertEquals("10", saved.getOwnerId());
        assertEquals("desc", saved.getDescription());
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), saved.getPlantDate());
        assertEquals(saved.getCode(), result.getCode());
    }

    @Test
    void createPlantationRetriesOnCodeCollisionAndEventuallySaves() {
        CreatePlantationRequest request = sampleCreateRequest();
        DataIntegrityViolationException collision = new DataIntegrityViolationException(
                "duplicate key",
                new RuntimeException("duplicate key value violates unique constraint plantations_code_key")
        );

        when(plantationRepository.save(any(Plantation.class)))
                .thenThrow(collision)
                .thenAnswer(invocation -> invocation.getArgument(0));

        Plantation result = plantationService.createPlantation(request);

        verify(plantationRepository, times(2)).save(any(Plantation.class));
        assertNotNull(result.getCode());
        assertTrue(result.getCode().matches("PLT-[A-F0-9]{8}"));
    }

    @Test
    void createPlantationThrowsAfterMaxRetries() {
        CreatePlantationRequest request = sampleCreateRequest();
        DataIntegrityViolationException collision = new DataIntegrityViolationException(
                "duplicate key value violates unique constraint plantations_code_key"
        );
        when(plantationRepository.save(any(Plantation.class))).thenThrow(collision);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> plantationService.createPlantation(request)
        );

        assertEquals("Failed to generate unique plantation code after 5 attempts", exception.getMessage());
        verify(plantationRepository, times(5)).save(any(Plantation.class));
    }

    @Test
    void createPlantationDoesNotRetryForNonCodeConstraintViolation() {
        CreatePlantationRequest request = sampleCreateRequest();
        DataIntegrityViolationException nonCodeViolation = new DataIntegrityViolationException(
                "duplicate key value violates unique constraint owner_id_key"
        );
        when(plantationRepository.save(any(Plantation.class))).thenThrow(nonCodeViolation);

        assertThrows(DataIntegrityViolationException.class, () -> plantationService.createPlantation(request));

        verify(plantationRepository, times(1)).save(any(Plantation.class));
    }

    @Test
    void createPlantationFromLegacyRequestMapsAndSaves() {
        PlantationRequest request = sampleLegacyRequest();
        when(plantationRepository.save(any(Plantation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Plantation result = plantationService.createPlantation(request);

        assertEquals("10", result.getOwnerId());
        assertTrue(result.getCode().matches("PLT-[A-F0-9]{8}"));
    }

    @Test
    void updatePlantationUpdatesAllowedFields() {
        Plantation existing = new Plantation();
        existing.setId(5L);
        existing.setCode("PLT-ABCDEF12");
        existing.setOwnerId("owner-1");
        existing.setName("Old Name");
        existing.setLocation("Old Location");
        existing.setArea(1.0);
        existing.setDescription("old");

        when(plantationRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(plantationRepository.save(any(Plantation.class))).thenAnswer(inv -> inv.getArgument(0));

        Plantation result = plantationService.updatePlantation(5L, sampleUpdateRequest());

        assertEquals("Updated Plantation", result.getName());
        assertEquals("Jambi", result.getLocation());
        assertEquals(10.0, result.getArea());
        assertEquals("new-desc", result.getDescription());
        assertEquals(LocalDateTime.of(2026, 2, 1, 0, 0), result.getPlantDate());
    }

    @Test
    void updatePlantationDoesNotModifyCodeAndOwnerIdFromLegacyRequest() {
        Plantation existing = new Plantation();
        existing.setId(5L);
        existing.setCode("PLT-ABCDEF12");
        existing.setOwnerId("owner-1");

        PlantationRequest request = sampleLegacyRequest();
        request.setOwnerId("owner-2");

        when(plantationRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(plantationRepository.save(any(Plantation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Plantation result = plantationService.updatePlantation(5L, request);

        assertEquals("PLT-ABCDEF12", result.getCode());
        assertEquals("owner-1", result.getOwnerId());
    }

    @Test
    void deletePlantationWorks() {
        Plantation plantation = new Plantation();
        when(plantationRepository.findById(5L)).thenReturn(Optional.of(plantation));

        plantationService.deletePlantation(5L);

        verify(plantationRepository).delete(plantation);
    }

    @Test
    void deletePlantationThrowsExceptionIfNotFound() {
        when(plantationRepository.findById(5L)).thenReturn(Optional.empty());

        PlantationNotFoundException exception = assertThrows(
                PlantationNotFoundException.class,
                () -> plantationService.deletePlantation(5L)
        );

        assertEquals("Plantation not found with id: 5", exception.getMessage());
        verify(plantationRepository, never()).delete(any());
    }

    private CreatePlantationRequest sampleCreateRequest() {
        CreatePlantationRequest request = new CreatePlantationRequest();
        request.setName("Plantation");
        request.setLocation("Riau");
        request.setArea(10.0);
        request.setOwnerId("10");
        request.setDescription("desc");
        request.setPlantDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        return request;
    }

    private UpdatePlantationRequest sampleUpdateRequest() {
        UpdatePlantationRequest request = new UpdatePlantationRequest();
        request.setName("Updated Plantation");
        request.setLocation("Jambi");
        request.setArea(10.0);
        request.setDescription("new-desc");
        request.setPlantDate(LocalDateTime.of(2026, 2, 1, 0, 0));
        return request;
    }

    private PlantationRequest sampleLegacyRequest() {
        PlantationRequest request = new PlantationRequest();
        request.setName("Plantation");
        request.setLocation("Riau");
        request.setArea(10.0);
        request.setOwnerId("10");
        request.setDescription("desc");
        request.setPlantDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        return request;
    }

    @Test
    void assignMandorSuccess() {
        Plantation plantation = new Plantation();
        plantation.setId(1L);
        
        when(plantationRepository.findByMandorId("mandor-1")).thenReturn(Optional.empty());
        when(plantationRepository.findById(1L)).thenReturn(Optional.of(plantation));
        when(plantationRepository.save(plantation)).thenReturn(plantation);

        Plantation result = plantationService.assignMandor(1L, "mandor-1");
        assertEquals("mandor-1", result.getMandorId());
        verify(plantationRepository).save(plantation);
    }

    @Test
    void assignMandorThrowsIfAlreadyAssigned() {
        Plantation existing = new Plantation();
        existing.setId(2L);
        when(plantationRepository.findByMandorId("mandor-1")).thenReturn(Optional.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            plantationService.assignMandor(1L, "mandor-1"));
        assertTrue(ex.getMessage().contains("already assigned"));
    }

    @Test
    void transferMandorSuccess() {
        Plantation source = new Plantation();
        source.setId(1L);
        source.setMandorId("mandor-1");

        Plantation target = new Plantation();
        target.setId(2L);

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(plantationRepository.findById(2L)).thenReturn(Optional.of(target));
        when(plantationRepository.save(source)).thenReturn(source);
        when(plantationRepository.save(target)).thenReturn(target);

        plantationService.transferMandor("mandor-1", 1L, 2L);

        assertNull(source.getMandorId());
        assertEquals("mandor-1", target.getMandorId());
        verify(plantationRepository).save(source);
        verify(plantationRepository).save(target);
    }

    @Test
    void transferMandorThrowsIfSourceNotMatching() {
        Plantation source = new Plantation();
        source.setId(1L);
        source.setMandorId("mandor-2");

        Plantation target = new Plantation();
        target.setId(2L);

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(plantationRepository.findById(2L)).thenReturn(Optional.of(target));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            plantationService.transferMandor("mandor-1", 1L, 2L));
        assertTrue(ex.getMessage().contains("not assigned to plantation"));
    }

    @Test
    void transferMandorThrowsIfSourceMandorNull() {
        Plantation source = new Plantation();
        source.setId(1L);

        Plantation target = new Plantation();
        target.setId(2L);

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(plantationRepository.findById(2L)).thenReturn(Optional.of(target));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            plantationService.transferMandor("mandor-1", 1L, 2L));
        assertTrue(ex.getMessage().contains("not assigned to plantation"));
    }

    @Test
    void transferMandorThrowsIfTargetAlreadyHasMandor() {
        Plantation source = new Plantation();
        source.setId(1L);
        source.setMandorId("mandor-1"); 

        Plantation target = new Plantation();
        target.setId(2L);
        target.setMandorId("mandor-3");

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(plantationRepository.findById(2L)).thenReturn(Optional.of(target));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            plantationService.transferMandor("mandor-1", 1L, 2L));
        assertTrue(ex.getMessage().contains("already has a mandor assigned"));
    }
}
