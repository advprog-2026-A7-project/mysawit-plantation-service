package com.mysawit.plantation.service;

import com.mysawit.plantation.dto.CreatePlantationRequest;
import com.mysawit.plantation.dto.PlantationRequest;
import com.mysawit.plantation.dto.UpdatePlantationRequest;
import com.mysawit.plantation.exception.PlantationNotFoundException;
import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.repository.PlantationRepository;
import com.mysawit.plantation.util.GeometryValidator;
import com.mysawit.plantation.exception.MandorAssignedException;
import com.mysawit.plantation.exception.InvalidGeometryException;
import com.mysawit.plantation.exception.OverlappingPlantationException;
import org.locationtech.jts.geom.Polygon;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlantationService {

    private static final String CODE_PREFIX = "PLT-";
    private static final int MAX_CODE_GENERATION_RETRIES = 5;
    private static final String CODE_GENERATION_ERROR =
            "Failed to generate unique plantation code after 5 attempts";

    private final PlantationRepository plantationRepository;
    private final GeometryValidator geometryValidator;

    public PlantationService(PlantationRepository plantationRepository, GeometryValidator geometryValidator) {
        this.plantationRepository = plantationRepository;
        this.geometryValidator = geometryValidator;
    }

    public List<Plantation> getAllPlantations() {
        return plantationRepository.findAll();
    }

    public Plantation getPlantationById(Long id) {
        return plantationRepository.findById(id)
                .orElseThrow(() -> new PlantationNotFoundException("Plantation not found with id: " + id));
    }

    public List<Plantation> getPlantationsByOwner(String ownerId) {
        return plantationRepository.findByOwnerId(ownerId);
    }

    public List<Plantation> getPlantationsByOwnerId(String ownerId) {
        return getPlantationsByOwner(ownerId);
    }

    public Plantation createPlantation(CreatePlantationRequest request) {
        validateGeometryAndOverlap(request.getCoordinates(), null);

        DataIntegrityViolationException lastCodeCollisionException = null;

        for (int attempt = 1; attempt <= MAX_CODE_GENERATION_RETRIES; attempt++) {
            Plantation plantation = new Plantation();
            plantation.setCode(generatePlantationCode());
            plantation.setName(request.getName());
            plantation.setLocation(request.getLocation());
            plantation.setArea(request.getArea());
            plantation.setOwnerId(request.getOwnerId());
            plantation.setDescription(request.getDescription());
            plantation.setPlantDate(request.getPlantDate());
            plantation.setCoordinates(request.getCoordinates());

            try {
                return plantationRepository.save(plantation);
            } catch (DataIntegrityViolationException exception) {
                if (!isCodeUniqueViolation(exception)) {
                    throw exception;
                }
                lastCodeCollisionException = exception;
            }
        }

        throw new IllegalStateException(CODE_GENERATION_ERROR, lastCodeCollisionException);
    }

    public Plantation createPlantation(PlantationRequest request) {
        CreatePlantationRequest createRequest = new CreatePlantationRequest();
        createRequest.setName(request.getName());
        createRequest.setLocation(request.getLocation());
        createRequest.setArea(request.getArea());
        createRequest.setOwnerId(request.getOwnerId());
        createRequest.setDescription(request.getDescription());
        createRequest.setPlantDate(request.getPlantDate());
        createRequest.setCoordinates(request.getCoordinates());
        return createPlantation(createRequest);
    }

    public Plantation updatePlantation(Long id, UpdatePlantationRequest request) {
        Plantation plantation = getPlantationById(id);

        validateGeometryAndOverlap(request.getCoordinates(), id);

        plantation.setName(request.getName());
        plantation.setLocation(request.getLocation());
        plantation.setArea(request.getArea());
        plantation.setDescription(request.getDescription());
        plantation.setPlantDate(request.getPlantDate());
        plantation.setCoordinates(request.getCoordinates());

        return plantationRepository.save(plantation);
    }

    public Plantation updatePlantation(Long id, PlantationRequest request) {
        UpdatePlantationRequest updateRequest = new UpdatePlantationRequest();
        updateRequest.setName(request.getName());
        updateRequest.setLocation(request.getLocation());
        updateRequest.setArea(request.getArea());
        updateRequest.setDescription(request.getDescription());
        updateRequest.setPlantDate(request.getPlantDate());
        updateRequest.setCoordinates(request.getCoordinates());
        return updatePlantation(id, updateRequest);
    }

    public void deletePlantation(Long id) {
        Plantation plantation = getPlantationById(id);
        if (plantation.getMandorId() != null) {
            throw new MandorAssignedException("Cannot delete plantation with ID " + id + " as it has an assigned mandor");
        }
        plantationRepository.delete(plantation);
    }

    @Transactional
    public Plantation assignMandor(Long id, String mandorId) {
        plantationRepository.findByMandorId(mandorId).ifPresent(p -> {
            throw new IllegalStateException("Mandor with ID " + mandorId + " is already assigned to plantation ID " + p.getId());
        });

        Plantation plantation = getPlantationById(id);
        plantation.setMandorId(mandorId);
        return plantationRepository.save(plantation);
    }

    @Transactional
    public void transferMandor(String mandorId, Long fromPlantationId, Long toPlantationId) {
        Plantation fromPlantation = getPlantationById(fromPlantationId);
        Plantation toPlantation = getPlantationById(toPlantationId);

        if (fromPlantation.getMandorId() == null || !fromPlantation.getMandorId().equals(mandorId)) {
            throw new IllegalStateException("Mandor with ID " + mandorId + " is not assigned to plantation ID " + fromPlantationId);
        }

        if (toPlantation.getMandorId() != null) {
            throw new IllegalStateException("Target plantation with ID " + toPlantationId + " already has a mandor assigned");
        }

        fromPlantation.setMandorId(null);
        toPlantation.setMandorId(mandorId);

        plantationRepository.save(fromPlantation);
        plantationRepository.save(toPlantation);
    }

    private String generatePlantationCode() {
        String randomCode = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
        return CODE_PREFIX + randomCode;
    }

    private boolean isCodeUniqueViolation(Throwable throwable) {
        while (throwable != null) {
            String message = throwable.getMessage();
            if (message != null) {
                String normalized = message.toLowerCase(Locale.ROOT);
                boolean mentionsCode = normalized.contains("code");
                boolean mentionsUniqueConstraint = normalized.contains("unique")
                        || normalized.contains("duplicate")
                        || normalized.contains("constraint");
                if (mentionsCode && mentionsUniqueConstraint) {
                    return true;
                }
            }
            throwable = throwable.getCause();
        }
        return false;
    }

    private void validateGeometryAndOverlap(List<com.mysawit.plantation.model.Coordinate> coordinates, Long excludeId) {
        if (!geometryValidator.isSquare(coordinates)) {
            throw new InvalidGeometryException("The provided coordinates do not form a valid square");
        }
        
        Polygon newPolygon = geometryValidator.createPolygon(coordinates);
        
        List<Plantation> existingPlantations = plantationRepository.findAll();
        for (Plantation existing : existingPlantations) {
            if (excludeId != null && excludeId.equals(existing.getId())) {
                continue;
            }
            if (existing.getCoordinates() == null || existing.getCoordinates().size() != 4) {
                continue;
            }
            try {
                Polygon existingPolygon = geometryValidator.createPolygon(existing.getCoordinates());
                if (newPolygon.intersects(existingPolygon)) {
                    throw new OverlappingPlantationException("Plantation overlaps with existing plantation ID: " + existing.getId());
                }
            } catch (Exception e) {
                // Ignore invalid geometries in DB during overlap check
            }
        }
    }
}
