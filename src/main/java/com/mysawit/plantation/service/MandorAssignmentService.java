package com.mysawit.plantation.service;

import com.mysawit.plantation.exception.PlantationNotFoundException;
import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.repository.PlantationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MandorAssignmentService {

    private final PlantationRepository plantationRepository;

    public MandorAssignmentService(PlantationRepository plantationRepository) {
        this.plantationRepository = plantationRepository;
    }

    @Transactional
    public Plantation assignMandor(Long plantationId, String mandorId) {
        plantationRepository.findByMandorId(mandorId).ifPresent(p -> {
            throw new IllegalStateException(
                    "Mandor with ID " + mandorId + " is already assigned to plantation ID " + p.getId());
        });

        Plantation plantation = findPlantation(plantationId);
        plantation.setMandorId(mandorId);
        return plantationRepository.save(plantation);
    }

    @Transactional
    public void transferMandor(String mandorId, Long fromPlantationId, Long toPlantationId) {
        Plantation fromPlantation = findPlantation(fromPlantationId);
        Plantation toPlantation = findPlantation(toPlantationId);

        if (fromPlantation.getMandorId() == null || !fromPlantation.getMandorId().equals(mandorId)) {
            throw new IllegalStateException(
                    "Mandor with ID " + mandorId + " is not assigned to plantation ID " + fromPlantationId);
        }

        if (toPlantation.getMandorId() != null) {
            throw new IllegalStateException(
                    "Target plantation with ID " + toPlantationId + " already has a mandor assigned");
        }

        fromPlantation.setMandorId(null);
        toPlantation.setMandorId(mandorId);

        plantationRepository.save(fromPlantation);
        plantationRepository.save(toPlantation);
    }

    private Plantation findPlantation(Long id) {
        return plantationRepository.findById(id)
                .orElseThrow(() -> new PlantationNotFoundException("Plantation not found with id: " + id));
    }
}
