package com.mysawit.plantation.exception;

import com.mysawit.plantation.controller.PlantationController;
import com.mysawit.plantation.service.PlantationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private PlantationService plantationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        plantationService = mock(PlantationService.class);
        PlantationController controller = new PlantationController(plantationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void plantationNotFoundExceptionIsMappedTo404() throws Exception {
        when(plantationService.getPlantationById(42L))
                .thenThrow(new PlantationNotFoundException("Plantation not found with id: 42"));

        mockMvc.perform(get("/api/plantations/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Plantation not found with id: 42"));
    }

    @Test
    void invalidGeometryExceptionIsMappedTo400() throws Exception {
        when(plantationService.getPlantationById(43L))
                .thenThrow(new InvalidGeometryException("Geometry is invalid"));

        mockMvc.perform(get("/api/plantations/43"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Geometry is invalid"));
    }
}
