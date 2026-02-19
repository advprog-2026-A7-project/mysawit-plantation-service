package com.mysawit.plantation.service;

import com.mysawit.plantation.dto.PlantationRequest;
import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.repository.PlantationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlantationService {
    
    private final PlantationRepository plantationRepository;
    
    public PlantationService(PlantationRepository plantationRepository) {
        this.plantationRepository = plantationRepository;
    }
    
    public List<Plantation> getAllPlantations() {
        return plantationRepository.findAll();
    }
    
    public Plantation getPlantationById(Long id) {
        return plantationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plantation not found with id: " + id));
    }
    
    public List<Plantation> getPlantationsByOwnerId(Long ownerId) {
        return plantationRepository.findByOwnerId(ownerId);
    }
    
    public Plantation createPlantation(PlantationRequest request) {
        Plantation plantation = new Plantation();
        plantation.setName(request.getName());
        plantation.setLocation(request.getLocation());
        plantation.setArea(request.getArea());
        plantation.setOwnerId(request.getOwnerId());
        plantation.setDescription(request.getDescription());
        plantation.setPlantDate(request.getPlantDate());
        
        return plantationRepository.save(plantation);
    }
    
    public Plantation updatePlantation(Long id, PlantationRequest request) {
        Plantation plantation = getPlantationById(id);
        
        plantation.setName(request.getName());
        plantation.setLocation(request.getLocation());
        plantation.setArea(request.getArea());
        plantation.setOwnerId(request.getOwnerId());
        plantation.setDescription(request.getDescription());
        plantation.setPlantDate(request.getPlantDate());
        
        return plantationRepository.save(plantation);
    }
    
    public void deletePlantation(Long id) {
        Plantation plantation = getPlantationById(id);
        plantationRepository.delete(plantation);
    }
}
