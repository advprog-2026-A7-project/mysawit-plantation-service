package com.mysawit.plantation.service;

import com.mysawit.plantation.dto.CreatePlantationRequest;
import com.mysawit.plantation.dto.PlantationRequest;
import com.mysawit.plantation.dto.UpdatePlantationRequest;
import com.mysawit.plantation.model.Plantation;
import org.springframework.stereotype.Component;

@Component
public class PlantationMapper {

    public Plantation buildPlantation(CreatePlantationRequest request, String code) {
        Plantation plantation = new Plantation();
        plantation.setCode(code);
        plantation.setName(request.getName());
        plantation.setLocation(request.getLocation());
        plantation.setArea(request.getArea());
        plantation.setOwnerId(request.getOwnerId());
        plantation.setDescription(request.getDescription());
        plantation.setPlantDate(request.getPlantDate());
        plantation.setCoordinates(request.getCoordinates());
        return plantation;
    }

    public CreatePlantationRequest toCreateRequest(PlantationRequest request) {
        CreatePlantationRequest createRequest = new CreatePlantationRequest();
        createRequest.setCode(request.getCode());
        createRequest.setName(request.getName());
        createRequest.setLocation(request.getLocation());
        createRequest.setArea(request.getArea());
        createRequest.setOwnerId(request.getOwnerId());
        createRequest.setDescription(request.getDescription());
        createRequest.setPlantDate(request.getPlantDate());
        createRequest.setCoordinates(request.getCoordinates());
        return createRequest;
    }

    public UpdatePlantationRequest toUpdateRequest(PlantationRequest request) {
        UpdatePlantationRequest updateRequest = new UpdatePlantationRequest();
        updateRequest.setName(request.getName());
        updateRequest.setLocation(request.getLocation());
        updateRequest.setArea(request.getArea());
        updateRequest.setDescription(request.getDescription());
        updateRequest.setPlantDate(request.getPlantDate());
        updateRequest.setCoordinates(request.getCoordinates());
        return updateRequest;
    }

    public void copyEditableFields(UpdatePlantationRequest request, Plantation plantation) {
        plantation.setName(request.getName());
        plantation.setLocation(request.getLocation());
        plantation.setArea(request.getArea());
        plantation.setDescription(request.getDescription());
        plantation.setPlantDate(request.getPlantDate());
        plantation.setCoordinates(request.getCoordinates());
    }
}
