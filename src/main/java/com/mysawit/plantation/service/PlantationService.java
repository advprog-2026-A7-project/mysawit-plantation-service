package com.mysawit.plantation.service;

import com.mysawit.plantation.dto.CreatePlantationRequest;
import com.mysawit.plantation.dto.PlantationRequest;
import com.mysawit.plantation.dto.UpdatePlantationRequest;
import com.mysawit.plantation.exception.MandorAssignedException;
import com.mysawit.plantation.exception.PlantationNotFoundException;
import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.repository.PlantationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlantationService {

    private static final int MAX_CODE_GENERATION_RETRIES = 5;
    private static final String CODE_GENERATION_ERROR =
            "Failed to generate unique plantation code after 5 attempts";

    private final PlantationRepository plantationRepository;
    private final PlantationMapper plantationMapper;
    private final PlantationCodeGenerator plantationCodeGenerator;
    private final PlantationGeometryService plantationGeometryService;
    private final MandorAssignmentService mandorAssignmentService;
    private final UniqueConstraintInspector uniqueConstraintInspector;
    private final PlantationEventPublisher eventPublisher;

    public PlantationService(PlantationRepository plantationRepository,
                             PlantationMapper plantationMapper,
                             PlantationCodeGenerator plantationCodeGenerator,
                             PlantationGeometryService plantationGeometryService,
                             MandorAssignmentService mandorAssignmentService,
                             UniqueConstraintInspector uniqueConstraintInspector,
                             PlantationEventPublisher eventPublisher) {
        this.plantationRepository = plantationRepository;
        this.plantationMapper = plantationMapper;
        this.plantationCodeGenerator = plantationCodeGenerator;
        this.plantationGeometryService = plantationGeometryService;
        this.mandorAssignmentService = mandorAssignmentService;
        this.uniqueConstraintInspector = uniqueConstraintInspector;
        this.eventPublisher = eventPublisher;
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
        plantationGeometryService.validateGeometryAndOverlap(request.getCoordinates(), null);
        return saveWithCodeRetry(request);
    }

    public Plantation createPlantation(PlantationRequest request) {
        return createPlantation(plantationMapper.toCreateRequest(request));
    }

    public Plantation updatePlantation(Long id, UpdatePlantationRequest request) {
        Plantation plantation = getPlantationById(id);
        plantationGeometryService.validateGeometryAndOverlap(request.getCoordinates(), id);
        plantationMapper.copyEditableFields(request, plantation);
        return plantationRepository.save(plantation);
    }

    public Plantation updatePlantation(Long id, PlantationRequest request) {
        return updatePlantation(id, plantationMapper.toUpdateRequest(request));
    }

    public void deletePlantation(Long id) {
        Plantation plantation = getPlantationById(id);
        if (plantation.getMandorId() != null) {
            throw new MandorAssignedException(
                    "Cannot delete plantation with ID " + id + " as it has an assigned mandor");
        }
        plantationRepository.delete(plantation);
    }

    public Plantation assignMandor(Long id, String mandorId) {
        Plantation result = mandorAssignmentService.assignMandor(id, mandorId);
        eventPublisher.publishMandorAssigned(id, mandorId);
        return result;
    }

    public Plantation unassignMandor(Long id) {
        Plantation plantation = getPlantationById(id);
        String previousMandorId = plantation.getMandorId();
        plantation.setMandorId(null);
        Plantation result = plantationRepository.save(plantation);
        eventPublisher.publishMandorUnassigned(id, previousMandorId);
        return result;
    }

    public void transferMandor(String mandorId, Long fromPlantationId, Long toPlantationId) {
        mandorAssignmentService.transferMandor(mandorId, fromPlantationId, toPlantationId);
    }

    public Plantation assignSupir(Long id, String supirId) {
        Plantation plantation = getPlantationById(id);
        plantation.addSupir(supirId);
        Plantation result = plantationRepository.save(plantation);
        eventPublisher.publishSupirAssigned(id, supirId);
        return result;
    }

    public Plantation unassignSupir(Long id, String supirId) {
        Plantation plantation = getPlantationById(id);
        plantation.removeSupir(supirId);
        Plantation result = plantationRepository.save(plantation);
        eventPublisher.publishSupirUnassigned(id, supirId);
        return result;
    }

    public java.util.Set<String> getSupirsByPlantation(Long id) {
        return getPlantationById(id).getSupirIds();
    }

    private Plantation saveWithCodeRetry(CreatePlantationRequest request) {
        DataIntegrityViolationException lastCodeCollisionException = null;

        for (int attempt = 1; attempt <= MAX_CODE_GENERATION_RETRIES; attempt++) {
            Plantation plantation = plantationMapper.buildPlantation(request, plantationCodeGenerator.generate());
            try {
                return plantationRepository.save(plantation);
            } catch (DataIntegrityViolationException exception) {
                if (!uniqueConstraintInspector.isCodeUniqueViolation(exception)) {
                    throw exception;
                }
                lastCodeCollisionException = exception;
            }
        }

        throw new IllegalStateException(CODE_GENERATION_ERROR, lastCodeCollisionException);
    }
}
