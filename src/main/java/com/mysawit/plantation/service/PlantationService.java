package com.mysawit.plantation.service;

import com.mysawit.plantation.dto.CreatePlantationRequest;
import com.mysawit.plantation.dto.PlantationRequest;
import com.mysawit.plantation.dto.UpdatePlantationRequest;
import com.mysawit.plantation.exception.PlantationNotFoundException;
import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.repository.PlantationRepository;
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

    public PlantationService(PlantationRepository plantationRepository) {
        this.plantationRepository = plantationRepository;
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
        return createPlantation(createRequest);
    }

    public Plantation updatePlantation(Long id, UpdatePlantationRequest request) {
        Plantation plantation = getPlantationById(id);

        plantation.setName(request.getName());
        plantation.setLocation(request.getLocation());
        plantation.setArea(request.getArea());
        plantation.setDescription(request.getDescription());
        plantation.setPlantDate(request.getPlantDate());

        return plantationRepository.save(plantation);
    }

    public Plantation updatePlantation(Long id, PlantationRequest request) {
        UpdatePlantationRequest updateRequest = new UpdatePlantationRequest();
        updateRequest.setName(request.getName());
        updateRequest.setLocation(request.getLocation());
        updateRequest.setArea(request.getArea());
        updateRequest.setDescription(request.getDescription());
        updateRequest.setPlantDate(request.getPlantDate());
        return updatePlantation(id, updateRequest);
    }

    public void deletePlantation(Long id) {
        Plantation plantation = getPlantationById(id);
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
}
