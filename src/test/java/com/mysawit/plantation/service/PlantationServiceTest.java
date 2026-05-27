package com.mysawit.plantation.service;

import com.mysawit.plantation.client.IdentityServiceClient;
import com.mysawit.plantation.dto.CreatePlantationRequest;
import com.mysawit.plantation.dto.PlantationRequest;
import com.mysawit.plantation.dto.SupirResponse;
import com.mysawit.plantation.dto.UpdatePlantationRequest;
import com.mysawit.plantation.exception.PlantationNotFoundException;
import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.model.Coordinate;
import com.mysawit.plantation.repository.PlantationRepository;
import com.mysawit.plantation.util.GeometryValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlantationServiceTest {

    @Mock
    private PlantationRepository plantationRepository;

    @Mock
    private PlantationEventPublisher eventPublisher;

    @Mock
    private IdentityServiceClient identityServiceClient;

    private final GeometryValidator geometryValidator = new GeometryValidator();

    private PlantationService plantationService;

    @BeforeEach
    void initService() {
        PlantationMapper plantationMapper = new PlantationMapper();
        PlantationCodeGenerator plantationCodeGenerator = new PlantationCodeGenerator();
        PlantationGeometryService plantationGeometryService =
                new PlantationGeometryService(geometryValidator, plantationRepository);
        MandorAssignmentService mandorAssignmentService =
                new MandorAssignmentService(plantationRepository);
        UniqueConstraintInspector uniqueConstraintInspector = new UniqueConstraintInspector();
        plantationService = new PlantationService(
                plantationRepository,
                plantationMapper,
                plantationCodeGenerator,
                plantationGeometryService,
                mandorAssignmentService,
                uniqueConstraintInspector,
                eventPublisher,
                identityServiceClient
        );
    }

    @Test
    void getAllPlantationsReturnsList() {
        when(plantationRepository.findAll()).thenReturn(List.of(new Plantation(), new Plantation()));

        List<Plantation> result = plantationService.getAllPlantations();

        assertEquals(2, result.size());
    }

    @Test
    void searchPlantationsDelegatesByProvidedFilters() {
        when(plantationRepository.findByNameContainingIgnoreCase("Alpha")).thenReturn(List.of(new Plantation()));
        when(plantationRepository.findByCodeContainingIgnoreCase("PLT")).thenReturn(List.of(new Plantation(), new Plantation()));
        when(plantationRepository.findByNameContainingIgnoreCaseAndCodeContainingIgnoreCase("Alpha", "PLT"))
                .thenReturn(List.of(new Plantation(), new Plantation(), new Plantation()));
        when(plantationRepository.findAll()).thenReturn(List.of());

        assertEquals(1, plantationService.searchPlantations(" Alpha ", null).size());
        assertEquals(2, plantationService.searchPlantations(null, " PLT ").size());
        assertEquals(3, plantationService.searchPlantations(" Alpha ", " PLT ").size());
        assertTrue(plantationService.searchPlantations(" ", "").isEmpty());
    }

    @Test
    void getPlantationByIdSuccess() {
        Plantation plantation = new Plantation();
        when(plantationRepository.findById(1L)).thenReturn(Optional.of(plantation));

        Plantation result = plantationService.getPlantationById(1L);

        assertSame(plantation, result);
    }

    @Test
    void getPlantationByIdThrowsExceptionWhenMissing() {
        when(plantationRepository.findById(1L)).thenReturn(Optional.empty());

        PlantationNotFoundException exception = assertThrows(
                PlantationNotFoundException.class,
                () -> plantationService.getPlantationById(1L)
        );

        assertEquals("Plantation not found with id: 1", exception.getMessage());
    }

    @Test
    void getPlantationsByOwnerReturnsRepositoryData() {
        when(plantationRepository.findByOwnerId("10")).thenReturn(List.of(new Plantation()));

        assertEquals(1, plantationService.getPlantationsByOwner("10").size());
    }

    @Test
    void getPlantationsByOwnerIdDelegatesToPrimaryMethod() {
        when(plantationRepository.findByOwnerId("10")).thenReturn(List.of(new Plantation()));

        assertEquals(1, plantationService.getPlantationsByOwnerId("10").size());
    }

    @Test
    void getPlantationsByMandorReturnsAssignedPlantations() {
        when(plantationRepository.findAllByMandorId("mandor-1")).thenReturn(List.of(new Plantation()));

        assertEquals(1, plantationService.getPlantationsByMandor("mandor-1").size());
    }

    @Test
    void createPlantationSavesEntityWithGeneratedCode() {
        CreatePlantationRequest request = sampleCreateRequest();
        when(plantationRepository.save(any(Plantation.class))).thenAnswer(inv -> inv.getArgument(0));

        Plantation result = plantationService.createPlantation(request);

        ArgumentCaptor<Plantation> captor = ArgumentCaptor.forClass(Plantation.class);
        verify(plantationRepository).save(captor.capture());
        Plantation saved = captor.getValue();

        assertNotNull(saved.getCode());
        assertTrue(saved.getCode().matches("PLT-[A-F0-9]{8}"));
        assertEquals("Plantation", saved.getName());
        assertEquals("Riau", saved.getLocation());
        assertEquals(10.0, saved.getArea());
        assertEquals("10", saved.getOwnerId());
        assertEquals("desc", saved.getDescription());
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), saved.getPlantDate());
        assertEquals(saved.getCode(), result.getCode());
    }

    @Test
    void createPlantationUsesRequestedCodeWhenProvided() {
        CreatePlantationRequest request = sampleCreateRequest();
        request.setCode("KB-A-001");
        when(plantationRepository.save(any(Plantation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Plantation result = plantationService.createPlantation(request);

        assertEquals("KB-A-001", result.getCode());
        verify(plantationRepository, times(1)).save(any(Plantation.class));
    }

    @Test
    void createPlantationWithRequestedDuplicateCodeReturnsClearError() {
        CreatePlantationRequest request = sampleCreateRequest();
        request.setCode("KB-A-001");
        DataIntegrityViolationException collision = new DataIntegrityViolationException(
                "duplicate key value violates unique constraint plantations_code_key"
        );
        when(plantationRepository.save(any(Plantation.class))).thenThrow(collision);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> plantationService.createPlantation(request)
        );

        assertEquals("Plantation code already exists", exception.getMessage());
        verify(plantationRepository, times(1)).save(any(Plantation.class));
    }

    @Test
    void createPlantationWithRequestedCodeRethrowsNonCodeConstraintViolation() {
        CreatePlantationRequest request = sampleCreateRequest();
        request.setCode("KB-A-001");
        DataIntegrityViolationException nonCodeViolation = new DataIntegrityViolationException(
                "duplicate key value violates unique constraint owner_id_key"
        );
        when(plantationRepository.save(any(Plantation.class))).thenThrow(nonCodeViolation);

        assertSame(
                nonCodeViolation,
                assertThrows(DataIntegrityViolationException.class, () -> plantationService.createPlantation(request))
        );
        verify(plantationRepository, times(1)).save(any(Plantation.class));
    }

    @Test
    void createPlantationRetriesOnCodeCollisionAndEventuallySaves() {
        CreatePlantationRequest request = sampleCreateRequest();
        DataIntegrityViolationException collision = new DataIntegrityViolationException(
                "duplicate key",
                new RuntimeException("duplicate key value violates unique constraint plantations_code_key")
        );

        when(plantationRepository.save(any(Plantation.class)))
                .thenThrow(collision)
                .thenAnswer(invocation -> invocation.getArgument(0));

        Plantation result = plantationService.createPlantation(request);

        verify(plantationRepository, times(2)).save(any(Plantation.class));
        assertNotNull(result.getCode());
        assertTrue(result.getCode().matches("PLT-[A-F0-9]{8}"));
    }

    @Test
    void createPlantationThrowsAfterMaxRetries() {
        CreatePlantationRequest request = sampleCreateRequest();
        DataIntegrityViolationException collision = new DataIntegrityViolationException(
                "duplicate key value violates unique constraint plantations_code_key"
        );
        when(plantationRepository.save(any(Plantation.class))).thenThrow(collision);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> plantationService.createPlantation(request)
        );

        assertEquals("Failed to generate unique plantation code after 5 attempts", exception.getMessage());
        verify(plantationRepository, times(5)).save(any(Plantation.class));
    }

    @Test
    void createPlantationDoesNotRetryForNonCodeConstraintViolation() {
        CreatePlantationRequest request = sampleCreateRequest();
        DataIntegrityViolationException nonCodeViolation = new DataIntegrityViolationException(
                "duplicate key value violates unique constraint owner_id_key"
        );
        when(plantationRepository.save(any(Plantation.class))).thenThrow(nonCodeViolation);

        assertThrows(DataIntegrityViolationException.class, () -> plantationService.createPlantation(request));

        verify(plantationRepository, times(1)).save(any(Plantation.class));
    }

    @Test
    void createPlantationDoesNotRetryWhenViolationMessageIsNull() {
        CreatePlantationRequest request = sampleCreateRequest();
        DataIntegrityViolationException violation = new DataIntegrityViolationException(
                null,
                new RuntimeException()
        );
        when(plantationRepository.save(any(Plantation.class))).thenThrow(violation);

        assertThrows(DataIntegrityViolationException.class, () -> plantationService.createPlantation(request));

        verify(plantationRepository, times(1)).save(any(Plantation.class));
    }

    @Test
    void createPlantationRetriesWhenDuplicateMessageMentionsCode() {
        CreatePlantationRequest request = sampleCreateRequest();
        DataIntegrityViolationException collision = new DataIntegrityViolationException("duplicate code value");

        when(plantationRepository.save(any(Plantation.class)))
                .thenThrow(collision)
                .thenAnswer(invocation -> invocation.getArgument(0));

        Plantation result = plantationService.createPlantation(request);

        assertNotNull(result.getCode());
        verify(plantationRepository, times(2)).save(any(Plantation.class));
    }

    @Test
    void createPlantationRetriesWhenConstraintMessageMentionsCode() {
        CreatePlantationRequest request = sampleCreateRequest();
        DataIntegrityViolationException collision = new DataIntegrityViolationException("code constraint violated");

        when(plantationRepository.save(any(Plantation.class)))
                .thenThrow(collision)
                .thenAnswer(invocation -> invocation.getArgument(0));

        Plantation result = plantationService.createPlantation(request);

        assertNotNull(result.getCode());
        verify(plantationRepository, times(2)).save(any(Plantation.class));
    }

    @Test
    void createPlantationFromLegacyRequestMapsAndSaves() {
        PlantationRequest request = sampleLegacyRequest();
        when(plantationRepository.save(any(Plantation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Plantation result = plantationService.createPlantation(request);

        assertEquals("10", result.getOwnerId());
        assertTrue(result.getCode().matches("PLT-[A-F0-9]{8}"));
    }

    @Test
    void updatePlantationUpdatesAllowedFields() {
        Plantation existing = new Plantation();
        existing.setId(5L);
        existing.setCode("PLT-ABCDEF12");
        existing.setOwnerId("owner-1");
        existing.setName("Old Name");
        existing.setLocation("Old Location");
        existing.setArea(1.0);
        existing.setDescription("old");

        when(plantationRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(plantationRepository.save(any(Plantation.class))).thenAnswer(inv -> inv.getArgument(0));

        Plantation result = plantationService.updatePlantation(5L, sampleUpdateRequest());

        assertEquals("Updated Plantation", result.getName());
        assertEquals("Jambi", result.getLocation());
        assertEquals(10.0, result.getArea());
        assertEquals("new-desc", result.getDescription());
        assertEquals(LocalDateTime.of(2026, 2, 1, 0, 0), result.getPlantDate());
    }

    @Test
    void updatePlantationDoesNotModifyCodeAndOwnerIdFromLegacyRequest() {
        Plantation existing = new Plantation();
        existing.setId(5L);
        existing.setCode("PLT-ABCDEF12");
        existing.setOwnerId("owner-1");

        PlantationRequest request = sampleLegacyRequest();
        request.setOwnerId("owner-2");

        when(plantationRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(plantationRepository.save(any(Plantation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Plantation result = plantationService.updatePlantation(5L, request);

        assertEquals("PLT-ABCDEF12", result.getCode());
        assertEquals("owner-1", result.getOwnerId());
    }

    @Test
    void deletePlantationWorks() {
        Plantation plantation = new Plantation();
        when(plantationRepository.findById(5L)).thenReturn(Optional.of(plantation));

        plantationService.deletePlantation(5L);

        verify(plantationRepository).delete(plantation);
    }

    @Test
    void deletePlantationThrowsExceptionIfNotFound() {
        when(plantationRepository.findById(5L)).thenReturn(Optional.empty());

        PlantationNotFoundException exception = assertThrows(
                PlantationNotFoundException.class,
                () -> plantationService.deletePlantation(5L)
        );

        assertEquals("Plantation not found with id: 5", exception.getMessage());
        verify(plantationRepository, never()).delete(any());
    }

    @Test
    void deletePlantationThrowsExceptionIfMandorAssigned() {
        Plantation plantation = new Plantation();
        plantation.setMandorId("mandor-1");
        when(plantationRepository.findById(5L)).thenReturn(Optional.of(plantation));

        com.mysawit.plantation.exception.MandorAssignedException exception = assertThrows(
                com.mysawit.plantation.exception.MandorAssignedException.class,
                () -> plantationService.deletePlantation(5L)
        );

        assertTrue(exception.getMessage().contains("Cannot delete plantation with ID 5 as it has an assigned mandor"));
        verify(plantationRepository, never()).delete(any());
    }

    @Test
    void createPlantationThrowsIfGeometryNotSquare() {
        CreatePlantationRequest request = sampleCreateRequest();
        request.setCoordinates(List.of(
            new Coordinate(0.0, 0.0),
            new Coordinate(0.0, 1.0),
            new Coordinate(2.0, 1.0),
            new Coordinate(2.0, 0.0)
        ));

        com.mysawit.plantation.exception.InvalidGeometryException exception = assertThrows(
                com.mysawit.plantation.exception.InvalidGeometryException.class,
                () -> plantationService.createPlantation(request)
        );
        assertTrue(exception.getMessage().contains("do not form a valid square"));
    }

    @Test
    void createPlantationThrowsIfOverlapping() {
        CreatePlantationRequest request = sampleCreateRequest();
        
        Plantation existing = new Plantation();
        existing.setId(99L);
        existing.setCoordinates(request.getCoordinates());
        when(plantationRepository.findAll()).thenReturn(List.of(existing));

        com.mysawit.plantation.exception.OverlappingPlantationException exception = assertThrows(
                com.mysawit.plantation.exception.OverlappingPlantationException.class,
                () -> plantationService.createPlantation(request)
        );
        assertTrue(exception.getMessage().contains("overlaps with existing plantation"));
    }

    @Test
    void createPlantationIgnoresIncompleteAndInvalidExistingGeometries() {
        CreatePlantationRequest request = sampleCreateRequest();

        Plantation incomplete = new Plantation();
        incomplete.setId(10L);
        incomplete.setCoordinates(List.of(
                new Coordinate(0.0, 0.0),
                new Coordinate(0.0, 1.0),
                new Coordinate(1.0, 1.0)
        ));

        Plantation invalidGeometry = new Plantation();
        invalidGeometry.setId(11L);
        invalidGeometry.setCoordinates(List.of(
                new Coordinate(10.0, 10.0),
                new Coordinate(11.0, 11.0),
                new Coordinate(10.0, 11.0),
                new Coordinate(11.0, 10.0)
        ));

        when(plantationRepository.findAll()).thenReturn(List.of(incomplete, invalidGeometry));
        when(plantationRepository.save(any(Plantation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Plantation result = plantationService.createPlantation(request);

        assertNotNull(result.getCode());
        verify(plantationRepository).save(any(Plantation.class));
    }

    @Test
    void updatePlantationSkipsOverlapCheckForExcludedPlantationId() {
        Plantation existing = new Plantation();
        existing.setId(5L);
        existing.setCode("PLT-ABCDEF12");
        existing.setOwnerId("owner-1");
        existing.setCoordinates(sampleUpdateRequest().getCoordinates());

        when(plantationRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(plantationRepository.findAll()).thenReturn(List.of(existing));
        when(plantationRepository.save(any(Plantation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Plantation result = plantationService.updatePlantation(5L, sampleUpdateRequest());

        assertEquals(5L, result.getId());
        verify(plantationRepository).save(existing);
    }

    private CreatePlantationRequest sampleCreateRequest() {
        CreatePlantationRequest request = new CreatePlantationRequest();
        request.setName("Plantation");
        request.setLocation("Riau");
        request.setArea(10.0);
        request.setOwnerId("10");
        request.setDescription("desc");
        request.setPlantDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        request.setCoordinates(List.of(
            new Coordinate(0.0, 0.0),
            new Coordinate(0.0, 1.0),
            new Coordinate(1.0, 1.0),
            new Coordinate(1.0, 0.0)
        ));
        return request;
    }

    private UpdatePlantationRequest sampleUpdateRequest() {
        UpdatePlantationRequest request = new UpdatePlantationRequest();
        request.setName("Updated Plantation");
        request.setLocation("Jambi");
        request.setArea(10.0);
        request.setDescription("new-desc");
        request.setPlantDate(LocalDateTime.of(2026, 2, 1, 0, 0));
        request.setCoordinates(List.of(
            new Coordinate(0.0, 0.0),
            new Coordinate(0.0, 1.0),
            new Coordinate(1.0, 1.0),
            new Coordinate(1.0, 0.0)
        ));
        return request;
    }

    private PlantationRequest sampleLegacyRequest() {
        PlantationRequest request = new PlantationRequest();
        request.setName("Plantation");
        request.setLocation("Riau");
        request.setArea(10.0);
        request.setOwnerId("10");
        request.setDescription("desc");
        request.setPlantDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        request.setCoordinates(List.of(
            new Coordinate(0.0, 0.0),
            new Coordinate(0.0, 1.0),
            new Coordinate(1.0, 1.0),
            new Coordinate(1.0, 0.0)
        ));
        return request;
    }

    @Test
    void assignMandorSuccess() {
        Plantation plantation = new Plantation();
        plantation.setId(1L);
        
        when(plantationRepository.findByMandorId("mandor-1")).thenReturn(Optional.empty());
        when(plantationRepository.findById(1L)).thenReturn(Optional.of(plantation));
        when(plantationRepository.save(plantation)).thenReturn(plantation);

        Plantation result = plantationService.assignMandor(1L, "mandor-1");
        assertEquals("mandor-1", result.getMandorId());
        verify(plantationRepository).save(plantation);
    }

    @Test
    void assignMandorThrowsIfAlreadyAssigned() {
        Plantation existing = new Plantation();
        existing.setId(2L);
        when(plantationRepository.findByMandorId("mandor-1")).thenReturn(Optional.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            plantationService.assignMandor(1L, "mandor-1"));
        assertTrue(ex.getMessage().contains("already assigned"));
    }

    @Test
    void transferMandorSuccess() {
        Plantation source = new Plantation();
        source.setId(1L);
        source.setMandorId("mandor-1");

        Plantation target = new Plantation();
        target.setId(2L);

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(plantationRepository.findById(2L)).thenReturn(Optional.of(target));
        when(plantationRepository.save(source)).thenReturn(source);
        when(plantationRepository.save(target)).thenReturn(target);

        plantationService.transferMandor("mandor-1", 1L, 2L);

        assertNull(source.getMandorId());
        assertEquals("mandor-1", target.getMandorId());
        verify(plantationRepository).save(source);
        verify(plantationRepository).save(target);
        verify(eventPublisher).publishMandorUnassigned(1L, "mandor-1");
        verify(eventPublisher).publishMandorAssigned(2L, "mandor-1");
    }

    @Test
    void transferMandorThrowsIfSourceNotMatching() {
        Plantation source = new Plantation();
        source.setId(1L);
        source.setMandorId("mandor-2");

        Plantation target = new Plantation();
        target.setId(2L);

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(plantationRepository.findById(2L)).thenReturn(Optional.of(target));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            plantationService.transferMandor("mandor-1", 1L, 2L));
        assertTrue(ex.getMessage().contains("not assigned to plantation"));
    }

    @Test
    void transferMandorThrowsIfSourceMandorNull() {
        Plantation source = new Plantation();
        source.setId(1L);

        Plantation target = new Plantation();
        target.setId(2L);

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(plantationRepository.findById(2L)).thenReturn(Optional.of(target));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            plantationService.transferMandor("mandor-1", 1L, 2L));
        assertTrue(ex.getMessage().contains("not assigned to plantation"));
    }

    @Test
    void transferMandorThrowsIfTargetAlreadyHasMandor() {
        Plantation source = new Plantation();
        source.setId(1L);
        source.setMandorId("mandor-1"); 

        Plantation target = new Plantation();
        target.setId(2L);
        target.setMandorId("mandor-3");

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(plantationRepository.findById(2L)).thenReturn(Optional.of(target));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            plantationService.transferMandor("mandor-1", 1L, 2L));
        assertTrue(ex.getMessage().contains("already has a mandor assigned"));
    }

    @Test
    void unassignMandorClearsMandorAndPublishesEvent() {
        Plantation plantation = new Plantation();
        plantation.setId(1L);
        plantation.setMandorId("mandor-1");

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(plantation));
        when(plantationRepository.save(plantation)).thenReturn(plantation);

        Plantation result = plantationService.unassignMandor(1L);

        assertNull(result.getMandorId());
        verify(plantationRepository).save(plantation);
        verify(eventPublisher).publishMandorUnassigned(1L, "mandor-1");
    }

    @Test
    void assignSupirAddsSupirAndPublishesEvent() {
        Plantation plantation = new Plantation();
        plantation.setId(1L);

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(plantation));
        when(plantationRepository.findBySupirIdsContaining("supir-1")).thenReturn(List.of());
        when(plantationRepository.save(plantation)).thenReturn(plantation);

        Plantation result = plantationService.assignSupir(1L, "supir-1");

        assertTrue(result.getSupirIds().contains("supir-1"));
        verify(plantationRepository).save(plantation);
        verify(eventPublisher).publishSupirAssigned(1L, "supir-1");
    }

    @Test
    void assignSupirThrowsIfAlreadyAssignedElsewhere() {
        Plantation current = new Plantation();
        current.setId(1L);
        Plantation other = new Plantation();
        other.setId(2L);
        other.addSupir("supir-1");

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(current));
        when(plantationRepository.findBySupirIdsContaining("supir-1")).thenReturn(List.of(other));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> plantationService.assignSupir(1L, "supir-1")
        );

        assertTrue(exception.getMessage().contains("already assigned"));
        verify(plantationRepository, never()).save(any(Plantation.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void unassignSupirRemovesSupirAndPublishesEvent() {
        Plantation plantation = new Plantation();
        plantation.setId(1L);
        plantation.addSupir("supir-1");

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(plantation));
        when(plantationRepository.save(plantation)).thenReturn(plantation);

        Plantation result = plantationService.unassignSupir(1L, "supir-1");

        assertFalse(result.getSupirIds().contains("supir-1"));
        verify(plantationRepository).save(plantation);
        verify(eventPublisher).publishSupirUnassigned(1L, "supir-1");
    }

    @Test
    void unassignSupirDoesNotPublishWhenSupirWasNotAssigned() {
        Plantation plantation = new Plantation();
        plantation.setId(1L);

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(plantation));
        when(plantationRepository.save(plantation)).thenReturn(plantation);

        Plantation result = plantationService.unassignSupir(1L, "supir-1");

        assertFalse(result.getSupirIds().contains("supir-1"));
        verify(plantationRepository).save(plantation);
        verify(eventPublisher, never()).publishSupirUnassigned(anyLong(), anyString());
    }

    @Test
    void getSupirsByPlantationReturnsAssignedSupirs() {
        Plantation plantation = new Plantation();
        plantation.addSupir("supir-1");

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(plantation));

        assertEquals(java.util.Set.of("supir-1"), plantationService.getSupirsByPlantation(1L));
    }

    @Test
    void getSupirDetailsByPlantationFiltersByName() {
        Plantation plantation = new Plantation();
        plantation.addSupir("supir-2");
        plantation.addSupir("supir-1");

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(plantation));
        when(identityServiceClient.getUserName("supir-1")).thenReturn("Budi Driver");
        when(identityServiceClient.getUserName("supir-2")).thenReturn("Sari Driver");

        List<SupirResponse> result = plantationService.getSupirDetailsByPlantation(1L, "budi");

        assertEquals(1, result.size());
        assertEquals("supir-1", result.get(0).id());
        assertEquals("Budi Driver", result.get(0).name());
    }

    @Test
    void transferSupirMovesSupirAndPublishesEvents() {
        Plantation source = new Plantation();
        source.setId(1L);
        source.addSupir("supir-1");
        Plantation target = new Plantation();
        target.setId(2L);

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(plantationRepository.findById(2L)).thenReturn(Optional.of(target));
        when(plantationRepository.save(source)).thenReturn(source);
        when(plantationRepository.save(target)).thenReturn(target);

        plantationService.transferSupir("supir-1", 1L, 2L);

        assertFalse(source.getSupirIds().contains("supir-1"));
        assertTrue(target.getSupirIds().contains("supir-1"));
        verify(eventPublisher).publishSupirUnassigned(1L, "supir-1");
        verify(eventPublisher).publishSupirAssigned(2L, "supir-1");
    }

    @Test
    void transferSupirRejectsSameSourceAndTarget() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> plantationService.transferSupir("supir-1", 1L, 1L)
        );

        assertTrue(exception.getMessage().contains("must be different"));
        verifyNoInteractions(plantationRepository);
    }

    @Test
    void transferSupirThrowsIfSourceDoesNotContainSupir() {
        Plantation source = new Plantation();
        source.setId(1L);
        Plantation target = new Plantation();
        target.setId(2L);

        when(plantationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(plantationRepository.findById(2L)).thenReturn(Optional.of(target));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> plantationService.transferSupir("supir-1", 1L, 2L)
        );

        assertTrue(exception.getMessage().contains("not assigned"));
        verify(plantationRepository, never()).save(any(Plantation.class));
    }

    @Test
    void syncAssignmentForUpdatedUserReturnsForBlankUserId() {
        plantationService.syncAssignmentForUpdatedUser(" ", "MANDOR");

        verifyNoInteractions(plantationRepository);
    }

    @Test
    void syncAssignmentForUpdatedMandorRepublishesCurrentAssignment() {
        Plantation plantation = new Plantation();
        plantation.setId(1L);
        plantation.setMandorId("mandor-1");

        when(plantationRepository.findByMandorId("mandor-1")).thenReturn(Optional.of(plantation));
        when(plantationRepository.findBySupirIdsContaining("mandor-1")).thenReturn(List.of());

        plantationService.syncAssignmentForUpdatedUser("mandor-1", " MANDOR ");

        verify(eventPublisher).publishMandorAssigned(1L, "mandor-1");
        verify(plantationRepository, never()).save(any(Plantation.class));
    }

    @Test
    void syncAssignmentForUpdatedSupirRepublishesCurrentAssignment() {
        Plantation plantation = new Plantation();
        plantation.setId(2L);
        plantation.addSupir("supir-1");

        when(plantationRepository.findByMandorId("supir-1")).thenReturn(Optional.empty());
        when(plantationRepository.findBySupirIdsContaining("supir-1")).thenReturn(List.of(plantation));

        plantationService.syncAssignmentForUpdatedUser("supir-1", "SUPIR");

        verify(eventPublisher).publishSupirAssigned(2L, "supir-1");
        verify(plantationRepository, never()).save(any(Plantation.class));
    }

    @Test
    void syncAssignmentForUpdatedNonWorkerRemovesAssignments() {
        Plantation mandorPlantation = new Plantation();
        mandorPlantation.setId(1L);
        mandorPlantation.setMandorId("user-1");

        Plantation supirPlantation = new Plantation();
        supirPlantation.setId(2L);
        supirPlantation.addSupir("user-1");

        when(plantationRepository.findByMandorId("user-1")).thenReturn(Optional.of(mandorPlantation));
        when(plantationRepository.findBySupirIdsContaining("user-1")).thenReturn(List.of(supirPlantation));
        when(plantationRepository.save(any(Plantation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        plantationService.syncAssignmentForUpdatedUser("user-1", "ADMIN");

        assertNull(mandorPlantation.getMandorId());
        assertFalse(supirPlantation.getSupirIds().contains("user-1"));
        verify(eventPublisher).publishMandorUnassigned(1L, "user-1");
        verify(eventPublisher).publishSupirUnassigned(2L, "user-1");
    }

    @Test
    void removeAssignmentsForDeletedUserReturnsForBlankUserId() {
        plantationService.removeAssignmentsForDeletedUser("");

        verifyNoInteractions(plantationRepository);
    }

    @Test
    void removeAssignmentsForDeletedUserClearsMandorAndSupirAssignments() {
        Plantation mandorPlantation = new Plantation();
        mandorPlantation.setId(1L);
        mandorPlantation.setMandorId("user-1");

        Plantation supirPlantation = new Plantation();
        supirPlantation.setId(2L);
        supirPlantation.addSupir("user-1");

        when(plantationRepository.findByMandorId("user-1")).thenReturn(Optional.of(mandorPlantation));
        when(plantationRepository.findBySupirIdsContaining("user-1")).thenReturn(List.of(supirPlantation));
        when(plantationRepository.save(any(Plantation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        plantationService.removeAssignmentsForDeletedUser("user-1");

        assertNull(mandorPlantation.getMandorId());
        assertFalse(supirPlantation.getSupirIds().contains("user-1"));
        verify(eventPublisher).publishMandorUnassigned(1L, "user-1");
        verify(eventPublisher).publishSupirUnassigned(2L, "user-1");
    }
}
