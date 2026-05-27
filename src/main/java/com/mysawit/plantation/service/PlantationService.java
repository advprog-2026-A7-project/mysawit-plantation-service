package com.mysawit.plantation.service;

import com.mysawit.plantation.client.IdentityServiceClient;
import com.mysawit.plantation.dto.CreatePlantationRequest;
import com.mysawit.plantation.dto.PlantationRequest;
import com.mysawit.plantation.dto.SupirResponse;
import com.mysawit.plantation.dto.UpdatePlantationRequest;
import com.mysawit.plantation.exception.MandorAssignedException;
import com.mysawit.plantation.exception.PlantationNotFoundException;
import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.repository.PlantationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class PlantationService {

    private static final int MAX_CODE_GENERATION_RETRIES = 5;
    private static final String CODE_GENERATION_ERROR =
            "Failed to generate unique plantation code after 5 attempts";
    private static final String ROLE_MANDOR = "MANDOR";
    private static final String ROLE_SUPIR = "SUPIR";

    private final PlantationRepository plantationRepository;
    private final PlantationMapper plantationMapper;
    private final PlantationCodeGenerator plantationCodeGenerator;
    private final PlantationGeometryService plantationGeometryService;
    private final MandorAssignmentService mandorAssignmentService;
    private final UniqueConstraintInspector uniqueConstraintInspector;
    private final PlantationEventPublisher eventPublisher;
    private final IdentityServiceClient identityServiceClient;

    public PlantationService(PlantationRepository plantationRepository,
                             PlantationMapper plantationMapper,
                             PlantationCodeGenerator plantationCodeGenerator,
                             PlantationGeometryService plantationGeometryService,
                             MandorAssignmentService mandorAssignmentService,
                             UniqueConstraintInspector uniqueConstraintInspector,
                             PlantationEventPublisher eventPublisher,
                             IdentityServiceClient identityServiceClient) {
        this.plantationRepository = plantationRepository;
        this.plantationMapper = plantationMapper;
        this.plantationCodeGenerator = plantationCodeGenerator;
        this.plantationGeometryService = plantationGeometryService;
        this.mandorAssignmentService = mandorAssignmentService;
        this.uniqueConstraintInspector = uniqueConstraintInspector;
        this.eventPublisher = eventPublisher;
        this.identityServiceClient = identityServiceClient;
    }

    public List<Plantation> getAllPlantations() {
        return plantationRepository.findAll();
    }

    public List<Plantation> searchPlantations(String name, String code) {
        boolean hasName = hasText(name);
        boolean hasCode = hasText(code);
        if (hasName && hasCode) {
            return plantationRepository.findByNameContainingIgnoreCaseAndCodeContainingIgnoreCase(name.trim(), code.trim());
        }
        if (hasName) {
            return plantationRepository.findByNameContainingIgnoreCase(name.trim());
        }
        if (hasCode) {
            return plantationRepository.findByCodeContainingIgnoreCase(code.trim());
        }
        return getAllPlantations();
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

    public List<Plantation> getPlantationsByMandor(String mandorId) {
        return plantationRepository.findAllByMandorId(mandorId);
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
        if (previousMandorId != null && !previousMandorId.isBlank()) {
            eventPublisher.publishMandorUnassigned(id, previousMandorId);
        }
        return result;
    }

    public void transferMandor(String mandorId, Long fromPlantationId, Long toPlantationId) {
        mandorAssignmentService.transferMandor(mandorId, fromPlantationId, toPlantationId);
        eventPublisher.publishMandorUnassigned(fromPlantationId, mandorId);
        eventPublisher.publishMandorAssigned(toPlantationId, mandorId);
    }

    public Plantation assignSupir(Long id, String supirId) {
        Plantation plantation = getPlantationById(id);
        plantationRepository.findBySupirIdsContaining(supirId).stream()
                .filter(existing -> !id.equals(existing.getId()))
                .findFirst()
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "Supir with ID " + supirId + " is already assigned to plantation ID " + existing.getId());
                });
        plantation.addSupir(supirId);
        Plantation result = plantationRepository.save(plantation);
        eventPublisher.publishSupirAssigned(id, supirId);
        return result;
    }

    public Plantation unassignSupir(Long id, String supirId) {
        Plantation plantation = getPlantationById(id);
        boolean wasAssigned = plantation.getSupirIds().contains(supirId);
        plantation.removeSupir(supirId);
        Plantation result = plantationRepository.save(plantation);
        if (wasAssigned) {
            eventPublisher.publishSupirUnassigned(id, supirId);
        }
        return result;
    }

    public java.util.Set<String> getSupirsByPlantation(Long id) {
        return getPlantationById(id).getSupirIds();
    }

    public List<SupirResponse> getSupirDetailsByPlantation(Long id, String name) {
        Set<String> supirIds = getPlantationById(id).getSupirIds();
        return supirIds.stream()
                .map(supirId -> new SupirResponse(supirId, identityServiceClient.getUserName(supirId)))
                .filter(supir -> !hasText(name)
                        || (supir.name() != null && supir.name().toLowerCase().contains(name.trim().toLowerCase())))
                .sorted(Comparator.comparing(SupirResponse::id))
                .toList();
    }

    @Transactional
    public void transferSupir(String supirId, Long fromPlantationId, Long toPlantationId) {
        if (fromPlantationId.equals(toPlantationId)) {
            throw new IllegalArgumentException("Source and target plantations must be different");
        }

        Plantation source = getPlantationById(fromPlantationId);
        Plantation target = getPlantationById(toPlantationId);

        if (!source.getSupirIds().contains(supirId)) {
            throw new IllegalStateException(
                    "Supir with ID " + supirId + " is not assigned to plantation ID " + fromPlantationId);
        }

        source.removeSupir(supirId);
        target.addSupir(supirId);

        plantationRepository.save(source);
        plantationRepository.save(target);
        eventPublisher.publishSupirUnassigned(fromPlantationId, supirId);
        eventPublisher.publishSupirAssigned(toPlantationId, supirId);
    }

    @Transactional
    public void syncAssignmentForUpdatedUser(String userId, String role) {
        if (!hasText(userId)) {
            return;
        }

        boolean shouldBeMandor = ROLE_MANDOR.equalsIgnoreCase(trim(role));
        boolean shouldBeSupir = ROLE_SUPIR.equalsIgnoreCase(trim(role));

        plantationRepository.findByMandorId(userId).ifPresent(plantation -> {
            if (shouldBeMandor) {
                eventPublisher.publishMandorAssigned(plantation.getId(), userId);
            } else {
                plantation.setMandorId(null);
                plantationRepository.save(plantation);
                eventPublisher.publishMandorUnassigned(plantation.getId(), userId);
            }
        });

        for (Plantation plantation : plantationRepository.findBySupirIdsContaining(userId)) {
            if (shouldBeSupir) {
                eventPublisher.publishSupirAssigned(plantation.getId(), userId);
            } else {
                plantation.removeSupir(userId);
                plantationRepository.save(plantation);
                eventPublisher.publishSupirUnassigned(plantation.getId(), userId);
            }
        }
    }

    @Transactional
    public void removeAssignmentsForDeletedUser(String userId) {
        if (!hasText(userId)) {
            return;
        }

        plantationRepository.findByMandorId(userId).ifPresent(plantation -> {
            plantation.setMandorId(null);
            plantationRepository.save(plantation);
            eventPublisher.publishMandorUnassigned(plantation.getId(), userId);
        });

        for (Plantation plantation : plantationRepository.findBySupirIdsContaining(userId)) {
            plantation.removeSupir(userId);
            plantationRepository.save(plantation);
            eventPublisher.publishSupirUnassigned(plantation.getId(), userId);
        }
    }

    private Plantation saveWithCodeRetry(CreatePlantationRequest request) {
        if (hasText(request.getCode())) {
            try {
                return plantationRepository.save(
                        plantationMapper.buildPlantation(request, request.getCode().trim()));
            } catch (DataIntegrityViolationException exception) {
                if (uniqueConstraintInspector.isCodeUniqueViolation(exception)) {
                    throw new IllegalArgumentException("Plantation code already exists");
                }
                throw exception;
            }
        }

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

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
