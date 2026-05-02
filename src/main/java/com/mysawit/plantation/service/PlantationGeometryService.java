package com.mysawit.plantation.service;

import com.mysawit.plantation.exception.InvalidGeometryException;
import com.mysawit.plantation.exception.OverlappingPlantationException;
import com.mysawit.plantation.model.Coordinate;
import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.repository.PlantationRepository;
import com.mysawit.plantation.util.GeometryValidator;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlantationGeometryService {

    private final GeometryValidator geometryValidator;
    private final PlantationRepository plantationRepository;

    public PlantationGeometryService(GeometryValidator geometryValidator,
                                     PlantationRepository plantationRepository) {
        this.geometryValidator = geometryValidator;
        this.plantationRepository = plantationRepository;
    }

    public void validateGeometryAndOverlap(List<Coordinate> coordinates, Long excludeId) {
        List<Plantation> existingPlantations = plantationRepository.findAll();

        if (!geometryValidator.isSquare(coordinates)) {
            throw new InvalidGeometryException("The provided coordinates do not form a valid square");
        }

        Polygon newPolygon = geometryValidator.createPolygon(coordinates);

        for (Plantation existing : existingPlantations) {
            if (excludeId != null && excludeId.equals(existing.getId())) {
                continue;
            }
            if (existing.getCoordinates() == null || existing.getCoordinates().size() != 4) {
                continue;
            }
            Polygon existingPolygon;
            try {
                existingPolygon = geometryValidator.createPolygon(existing.getCoordinates());
            } catch (Exception e) {
                continue;
            }
            if (newPolygon.intersects(existingPolygon)) {
                throw new OverlappingPlantationException(
                        "Plantation overlaps with existing plantation ID: " + existing.getId());
            }
        }
    }
}
