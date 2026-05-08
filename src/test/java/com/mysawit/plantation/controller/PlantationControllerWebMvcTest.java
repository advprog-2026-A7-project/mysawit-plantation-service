package com.mysawit.plantation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysawit.plantation.dto.AssignMandorRequest;
import com.mysawit.plantation.dto.CreatePlantationRequest;
import com.mysawit.plantation.dto.TransferMandorRequest;
import com.mysawit.plantation.dto.UpdatePlantationRequest;
import com.mysawit.plantation.exception.PlantationNotFoundException;
import com.mysawit.plantation.model.Plantation;
import com.mysawit.plantation.model.Coordinate;
import com.mysawit.plantation.service.PlantationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import com.mysawit.plantation.security.JwtAuthenticationFilter;
import com.mysawit.plantation.security.JwtUtil;
import com.mysawit.plantation.security.SecurityConfig;

@WebMvcTest(PlantationController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
@WithMockUser(roles = "ADMIN")
class PlantationControllerWebMvcTest {

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlantationService plantationService;

    private Plantation samplePlantation;
    private CreatePlantationRequest validCreateRequest;
    private UpdatePlantationRequest validUpdateRequest;

    @BeforeEach
    void setUp() {
        samplePlantation = new Plantation();
        samplePlantation.setId(1L);
        samplePlantation.setCode("PLT-12345678");
        samplePlantation.setName("Test Plantation");
        samplePlantation.setLocation("Location A");
        samplePlantation.setArea(150.5);
        samplePlantation.setDescription("Description X");
        samplePlantation.setOwnerId("Owner-1");
        samplePlantation.setPlantDate(LocalDateTime.of(2025, 1, 1, 0, 0));

        validCreateRequest = new CreatePlantationRequest();
        validCreateRequest.setName("Test Plantation");
        validCreateRequest.setLocation("Location A");
        validCreateRequest.setArea(150.5);
        validCreateRequest.setOwnerId("Owner-1");
        validCreateRequest.setDescription("Description X");
        validCreateRequest.setPlantDate(LocalDateTime.of(2025, 1, 1, 0, 0));
        validCreateRequest.setCoordinates(List.of(
            new Coordinate(0.0, 0.0),
            new Coordinate(0.0, 1.0),
            new Coordinate(1.0, 1.0),
            new Coordinate(1.0, 0.0)
        ));

        validUpdateRequest = new UpdatePlantationRequest();
        validUpdateRequest.setName("Test Plantation");
        validUpdateRequest.setLocation("Location A");
        validUpdateRequest.setArea(150.5);
        validUpdateRequest.setDescription("Description X");
        validUpdateRequest.setPlantDate(LocalDateTime.of(2025, 1, 1, 0, 0));
        validUpdateRequest.setCoordinates(List.of(
            new Coordinate(0.0, 0.0),
            new Coordinate(0.0, 1.0),
            new Coordinate(1.0, 1.0),
            new Coordinate(1.0, 0.0)
        ));
    }

    @Test
    void getAllPlantations_Returns200AndList() throws Exception {
        when(plantationService.getAllPlantations()).thenReturn(List.of(samplePlantation));

        mockMvc.perform(get("/api/plantations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].code", is("PLT-12345678")))
                .andExpect(jsonPath("$[0].name", is("Test Plantation")));
    }

    @Test
    void getPlantationById_Returns200AndEntity() throws Exception {
        when(plantationService.getPlantationById(1L)).thenReturn(samplePlantation);

        mockMvc.perform(get("/api/plantations/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.code", is("PLT-12345678")));
    }

    @Test
    void getPlantationById_ReturnsNotFoundWhenMissing() throws Exception {
        when(plantationService.getPlantationById(99L)).thenThrow(new PlantationNotFoundException("Plantation not found"));

        mockMvc.perform(get("/api/plantations/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPlantationsByOwnerId_Returns200AndList() throws Exception {
        when(plantationService.getPlantationsByOwner("Owner-1")).thenReturn(List.of(samplePlantation));

        mockMvc.perform(get("/api/plantations/owner/Owner-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ownerId", is("Owner-1")));
    }

    @Test
    void createPlantation_Returns201AndEntity() throws Exception {
        when(plantationService.createPlantation(any(CreatePlantationRequest.class))).thenReturn(samplePlantation);

        mockMvc.perform(post("/api/plantations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Plantation")));
    }

    @Test
    void createPlantation_Returns400OnValidationFailure() throws Exception {
        validCreateRequest.setName(""); // Blank name violates @NotBlank

        mockMvc.perform(post("/api/plantations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPlantation_Returns400WhenLatitudeOutsideRange() throws Exception {
        validCreateRequest.setCoordinates(List.of(
                new Coordinate(91.0000, 101.4000),
                new Coordinate(91.0000, 101.4010),
                new Coordinate(91.0010, 101.4010),
                new Coordinate(91.0010, 101.4000)
        ));

        mockMvc.perform(post("/api/plantations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPlantation_Returns400WhenLongitudeOutsideRange() throws Exception {
        validCreateRequest.setCoordinates(List.of(
                new Coordinate(-0.5000, 181.0000),
                new Coordinate(-0.5000, 181.0010),
                new Coordinate(-0.4990, 181.0010),
                new Coordinate(-0.4990, 181.0000)
        ));

        mockMvc.perform(post("/api/plantations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePlantation_Returns200AndEntity() throws Exception {
        when(plantationService.updatePlantation(eq(1L), any(UpdatePlantationRequest.class))).thenReturn(samplePlantation);

        mockMvc.perform(put("/api/plantations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void updatePlantation_Returns404WhenMissing() throws Exception {
        when(plantationService.updatePlantation(eq(99L), any(UpdatePlantationRequest.class)))
                .thenThrow(new PlantationNotFoundException("Plantation not found with id: 99"));

        mockMvc.perform(put("/api/plantations/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Plantation not found with id: 99")));
    }

    @Test
    void deletePlantation_Returns204NoContent() throws Exception {
        doNothing().when(plantationService).deletePlantation(1L);

        mockMvc.perform(delete("/api/plantations/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePlantation_Returns404WhenMissing() throws Exception {
        doThrow(new PlantationNotFoundException("Plantation not found with id: 99"))
                .when(plantationService)
                .deletePlantation(99L);

        mockMvc.perform(delete("/api/plantations/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Plantation not found with id: 99")));
    }

    @Test
    void assignMandor_Returns200AndEntity() throws Exception {
        AssignMandorRequest request = new AssignMandorRequest();
        request.setMandorId("mandor-1");
        
        Plantation p = new Plantation();
        p.setId(1L);
        p.setMandorId("mandor-1");

        when(plantationService.assignMandor(1L, "mandor-1")).thenReturn(p);

        mockMvc.perform(post("/api/plantations/1/mandor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mandorId", is("mandor-1")));
    }

    @Test
    void transferMandor_Returns200() throws Exception {
        TransferMandorRequest request = new TransferMandorRequest();
        request.setMandorId("mandor-1");
        request.setFromPlantationId(1L);
        request.setToPlantationId(2L);

        doNothing().when(plantationService).transferMandor("mandor-1", 1L, 2L);

        mockMvc.perform(put("/api/plantations/transfer-mandor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
