package com.mysawit.plantation.service;

import com.mysawit.plantation.dto.CreatePlantationRequest;
import com.mysawit.plantation.dto.PlantationRequest;
import com.mysawit.plantation.dto.UpdatePlantationRequest;
import com.mysawit.plantation.model.Coordinate;
import com.mysawit.plantation.model.Plantation;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlantationMapperTest {

    private final PlantationMapper mapper = new PlantationMapper();

    @Test
    void buildPlantationCopiesAllFieldsAndAssignsCode() {
        CreatePlantationRequest request = sampleCreateRequest();

        Plantation plantation = mapper.buildPlantation(request, "PLT-ABCDEF12");

        assertEquals("PLT-ABCDEF12", plantation.getCode());
        assertEquals("Plantation", plantation.getName());
        assertEquals("Riau", plantation.getLocation());
        assertEquals(10.0, plantation.getArea());
        assertEquals("owner-1", plantation.getOwnerId());
        assertEquals("desc", plantation.getDescription());
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), plantation.getPlantDate());
        assertEquals(request.getCoordinates(), plantation.getCoordinates());
    }

    @Test
    void toCreateRequestCopiesEveryField() {
        PlantationRequest request = sampleLegacyRequest();

        CreatePlantationRequest result = mapper.toCreateRequest(request);

        assertEquals(request.getName(), result.getName());
        assertEquals(request.getCode(), result.getCode());
        assertEquals(request.getLocation(), result.getLocation());
        assertEquals(request.getArea(), result.getArea());
        assertEquals(request.getOwnerId(), result.getOwnerId());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(request.getPlantDate(), result.getPlantDate());
        assertEquals(request.getCoordinates(), result.getCoordinates());
    }

    @Test
    void toUpdateRequestCopiesEditableFieldsAndOmitsOwnerId() {
        PlantationRequest request = sampleLegacyRequest();

        UpdatePlantationRequest result = mapper.toUpdateRequest(request);

        assertEquals(request.getName(), result.getName());
        assertEquals(request.getLocation(), result.getLocation());
        assertEquals(request.getArea(), result.getArea());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(request.getPlantDate(), result.getPlantDate());
        assertEquals(request.getCoordinates(), result.getCoordinates());
    }

    @Test
    void copyEditableFieldsDoesNotChangeCodeOrOwnerId() {
        Plantation plantation = new Plantation();
        plantation.setCode("PLT-EXISTING");
        plantation.setOwnerId("owner-original");

        UpdatePlantationRequest update = new UpdatePlantationRequest();
        update.setName("Updated");
        update.setLocation("Jambi");
        update.setArea(20.0);
        update.setDescription("new-desc");
        update.setPlantDate(LocalDateTime.of(2026, 2, 1, 0, 0));
        update.setCoordinates(sampleCoordinates());

        mapper.copyEditableFields(update, plantation);

        assertEquals("Updated", plantation.getName());
        assertEquals("Jambi", plantation.getLocation());
        assertEquals(20.0, plantation.getArea());
        assertEquals("new-desc", plantation.getDescription());
        assertEquals(LocalDateTime.of(2026, 2, 1, 0, 0), plantation.getPlantDate());
        assertEquals(update.getCoordinates(), plantation.getCoordinates());
        assertEquals("PLT-EXISTING", plantation.getCode());
        assertEquals("owner-original", plantation.getOwnerId());
    }

    @Test
    void copyEditableFieldsAcceptsNullOptionalFields() {
        Plantation plantation = new Plantation();
        plantation.setCode("PLT-EXISTING");
        plantation.setOwnerId("owner-original");

        UpdatePlantationRequest update = new UpdatePlantationRequest();
        update.setName("Name");
        update.setLocation("Location");
        update.setArea(1.0);
        update.setCoordinates(sampleCoordinates());

        mapper.copyEditableFields(update, plantation);

        assertNull(plantation.getDescription());
        assertNull(plantation.getPlantDate());
        assertEquals("PLT-EXISTING", plantation.getCode());
        assertEquals("owner-original", plantation.getOwnerId());
    }

    private CreatePlantationRequest sampleCreateRequest() {
        CreatePlantationRequest request = new CreatePlantationRequest();
        request.setName("Plantation");
        request.setLocation("Riau");
        request.setArea(10.0);
        request.setOwnerId("owner-1");
        request.setDescription("desc");
        request.setPlantDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        request.setCoordinates(sampleCoordinates());
        return request;
    }

    private PlantationRequest sampleLegacyRequest() {
        PlantationRequest request = new PlantationRequest();
        request.setCode("KB-A-001");
        request.setName("Plantation");
        request.setLocation("Riau");
        request.setArea(10.0);
        request.setOwnerId("owner-1");
        request.setDescription("desc");
        request.setPlantDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        request.setCoordinates(sampleCoordinates());
        return request;
    }

    private List<Coordinate> sampleCoordinates() {
        return List.of(
                new Coordinate(0.0, 0.0),
                new Coordinate(0.0, 1.0),
                new Coordinate(1.0, 1.0),
                new Coordinate(1.0, 0.0)
        );
    }
}
