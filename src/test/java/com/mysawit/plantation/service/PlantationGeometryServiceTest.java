package com.mysawit.plantation.service;

import com.mysawit.plantation.exception.InvalidGeometryException;
import com.mysawit.plantation.exception.OverlappingPlantationException;
import com.mysawit.plantation.model.Coordinate;
import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.repository.PlantationRepository;
import com.mysawit.plantation.util.GeometryValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlantationGeometryServiceTest {

    @Mock
    private PlantationRepository plantationRepository;

    private PlantationGeometryService plantationGeometryService;

    @BeforeEach
    void setUp() {
        plantationGeometryService = new PlantationGeometryService(
                new GeometryValidator(),
                plantationRepository
        );
    }

    @Test
    void validateGeometryAndOverlapAcceptsValidRealWorldSquareWhenNoOverlap() {
        when(plantationRepository.findAll()).thenReturn(List.of());

        assertDoesNotThrow(() -> plantationGeometryService.validateGeometryAndOverlap(
                realWorldSquare(),
                null
        ));
    }

    @Test
    void validateGeometryAndOverlapRejectsRealWorldPartiallyOverlappingSquare() {
        Plantation existing = plantationWithCoordinates(99L, realWorldSquare());
        when(plantationRepository.findAll()).thenReturn(List.of(existing));

        OverlappingPlantationException exception = assertThrows(
                OverlappingPlantationException.class,
                () -> plantationGeometryService.validateGeometryAndOverlap(overlappingRealWorldSquare(), null)
        );

        assertTrue(exception.getMessage().contains("overlaps with existing plantation ID"));
    }

    @Test
    void validateGeometryAndOverlapAllowsRealWorldNonOverlappingSquare() {
        Plantation existing = plantationWithCoordinates(99L, realWorldSquare());
        when(plantationRepository.findAll()).thenReturn(List.of(existing));

        assertDoesNotThrow(() -> plantationGeometryService.validateGeometryAndOverlap(
                nonOverlappingRealWorldSquare(),
                null
        ));
    }

    @Test
    void validateGeometryAndOverlapRejectsNonSquareRealWorldCoordinates() {
        assertThrows(
                InvalidGeometryException.class,
                () -> plantationGeometryService.validateGeometryAndOverlap(nonSquareRealWorldCoordinates(), null)
        );
    }

    @Test
    void validateGeometryAndOverlapRejectsLatitudeOutsideWorldRange() {
        when(plantationRepository.findAll()).thenReturn(List.of());

        assertThrows(
                InvalidGeometryException.class,
                () -> plantationGeometryService.validateGeometryAndOverlap(latitudeOutsideWorldRange(), null)
        );
    }

    @Test
    void validateGeometryAndOverlapRejectsLongitudeOutsideWorldRange() {
        when(plantationRepository.findAll()).thenReturn(List.of());

        assertThrows(
                InvalidGeometryException.class,
                () -> plantationGeometryService.validateGeometryAndOverlap(longitudeOutsideWorldRange(), null)
        );
    }

    private Plantation plantationWithCoordinates(Long id, List<Coordinate> coordinates) {
        Plantation plantation = new Plantation();
        plantation.setId(id);
        plantation.setCoordinates(coordinates);
        return plantation;
    }

    private List<Coordinate> realWorldSquare() {
        return List.of(
                new Coordinate(-0.5000, 101.4000),
                new Coordinate(-0.5000, 101.4010),
                new Coordinate(-0.4990, 101.4010),
                new Coordinate(-0.4990, 101.4000)
        );
    }

    private List<Coordinate> overlappingRealWorldSquare() {
        return List.of(
                new Coordinate(-0.4995, 101.4005),
                new Coordinate(-0.4995, 101.4015),
                new Coordinate(-0.4985, 101.4015),
                new Coordinate(-0.4985, 101.4005)
        );
    }

    private List<Coordinate> nonOverlappingRealWorldSquare() {
        return List.of(
                new Coordinate(-0.5100, 101.4100),
                new Coordinate(-0.5100, 101.4110),
                new Coordinate(-0.5090, 101.4110),
                new Coordinate(-0.5090, 101.4100)
        );
    }

    private List<Coordinate> nonSquareRealWorldCoordinates() {
        return List.of(
                new Coordinate(-0.5000, 101.4000),
                new Coordinate(-0.5000, 101.4020),
                new Coordinate(-0.4990, 101.4020),
                new Coordinate(-0.4990, 101.4000)
        );
    }

    private List<Coordinate> latitudeOutsideWorldRange() {
        return List.of(
                new Coordinate(91.0000, 101.4000),
                new Coordinate(91.0000, 101.4010),
                new Coordinate(91.0010, 101.4010),
                new Coordinate(91.0010, 101.4000)
        );
    }

    private List<Coordinate> longitudeOutsideWorldRange() {
        return List.of(
                new Coordinate(-0.5000, 181.0000),
                new Coordinate(-0.5000, 181.0010),
                new Coordinate(-0.4990, 181.0010),
                new Coordinate(-0.4990, 181.0000)
        );
    }
}
