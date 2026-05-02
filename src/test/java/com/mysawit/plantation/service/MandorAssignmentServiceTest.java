package com.mysawit.plantation.service;

import com.mysawit.plantation.exception.PlantationNotFoundException;
import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.repository.PlantationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MandorAssignmentServiceTest {

    @Mock
    private PlantationRepository plantationRepository;

    @InjectMocks
    private MandorAssignmentService mandorAssignmentService;

    @Test
    void assignMandorAttachesMandorToPlantation() {
        Plantation plantation = new Plantation();
        plantation.setId(1L);

        when(plantationRepository.findByMandorId("mandor-1")).thenReturn(Optional.empty());
        when(plantationRepository.findById(1L)).thenReturn(Optional.of(plantation));
        when(plantationRepository.save(plantation)).thenReturn(plantation);

        Plantation result = mandorAssignmentService.assignMandor(1L, "mandor-1");

        assertEquals("mandor-1", result.getMandorId());
        verify(plantationRepository).save(plantation);
    }

    @Test
    void assignMandorThrowsWhenMandorAlreadyAssignedElsewhere() {
        Plantation existing = new Plantation();
        existing.setId(2L);
        when(plantationRepository.findByMandorId("mandor-1")).thenReturn(Optional.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> mandorAssignmentService.assignMandor(1L, "mandor-1"));
        assertTrue(ex.getMessage().contains("already assigned"));
        verify(plantationRepository, never()).save(any(Plantation.class));
    }

    @Test
    void assignMandorThrowsWhenPlantationNotFound() {
        when(plantationRepository.findByMandorId("mandor-1")).thenReturn(Optional.empty());
        when(plantationRepository.findById(99L)).thenReturn(Optional.empty());

        PlantationNotFoundException ex = assertThrows(PlantationNotFoundException.class,
                () -> mandorAssignmentService.assignMandor(99L, "mandor-1"));
        assertEquals("Plantation not found with id: 99", ex.getMessage());
        verify(plantationRepository, never()).save(any(Plantation.class));
    }

    @Test
    void transferMandorMovesAssignmentToTarget() {
        Plantation source = new Plantation();
        source.setId(1L);
        source.setMandorId("mandor-1");

        Plantation target = new Plantation();
        target.setId(2L);

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(plantationRepository.findById(2L)).thenReturn(Optional.of(target));
        when(plantationRepository.save(source)).thenReturn(source);
        when(plantationRepository.save(target)).thenReturn(target);

        mandorAssignmentService.transferMandor("mandor-1", 1L, 2L);

        assertNull(source.getMandorId());
        assertEquals("mandor-1", target.getMandorId());
        verify(plantationRepository).save(source);
        verify(plantationRepository).save(target);
    }

    @Test
    void transferMandorThrowsWhenSourcePlantationNotFound() {
        when(plantationRepository.findById(1L)).thenReturn(Optional.empty());

        PlantationNotFoundException ex = assertThrows(PlantationNotFoundException.class,
                () -> mandorAssignmentService.transferMandor("mandor-1", 1L, 2L));
        assertEquals("Plantation not found with id: 1", ex.getMessage());
    }

    @Test
    void transferMandorThrowsWhenSourceMandorMismatch() {
        Plantation source = new Plantation();
        source.setId(1L);
        source.setMandorId("mandor-2");

        Plantation target = new Plantation();
        target.setId(2L);

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(plantationRepository.findById(2L)).thenReturn(Optional.of(target));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> mandorAssignmentService.transferMandor("mandor-1", 1L, 2L));
        assertTrue(ex.getMessage().contains("not assigned to plantation"));
        verify(plantationRepository, never()).save(any(Plantation.class));
    }

    @Test
    void transferMandorThrowsWhenTargetAlreadyHasMandor() {
        Plantation source = new Plantation();
        source.setId(1L);
        source.setMandorId("mandor-1");

        Plantation target = new Plantation();
        target.setId(2L);
        target.setMandorId("mandor-3");

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(plantationRepository.findById(2L)).thenReturn(Optional.of(target));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> mandorAssignmentService.transferMandor("mandor-1", 1L, 2L));
        assertTrue(ex.getMessage().contains("already has a mandor assigned"));
        verify(plantationRepository, never()).save(any(Plantation.class));
    }

}
