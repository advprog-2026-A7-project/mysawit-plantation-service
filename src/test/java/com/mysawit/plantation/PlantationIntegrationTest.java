package com.mysawit.plantation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysawit.plantation.repository.PlantationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlantationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlantationRepository plantationRepository;

    @BeforeEach
    void cleanDatabase() {
        plantationRepository.deleteAll();
    }

    @Test
    void plantationCrudFlowWorksEndToEnd() throws Exception {
        String created = mockMvc.perform(post("/api/plantations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Block A",
                                  "location": "Riau",
                                  "area": 12.5,
                                  "ownerId": 42,
                                  "description": "Prime block",
                                  "plantDate": "2026-05-22T08:00:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Block A"))
                .andExpect(jsonPath("$.location").value("Riau"))
                .andExpect(jsonPath("$.area").value(12.5))
                .andExpect(jsonPath("$.ownerId").value(42))
                .andExpect(jsonPath("$.description").value("Prime block"))
                .andExpect(jsonPath("$.plantDate").value("2026-05-22T08:00:00"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(get("/api/plantations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/plantations").param("ownerId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id));

        mockMvc.perform(get("/api/plantations/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(put("/api/plantations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Block B",
                                  "location": "Jambi",
                                  "area": 18.75,
                                  "ownerId": 77,
                                  "description": "Updated block",
                                  "plantDate": "2026-06-01T09:30:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Block B"))
                .andExpect(jsonPath("$.location").value("Jambi"))
                .andExpect(jsonPath("$.area").value(18.75))
                .andExpect(jsonPath("$.ownerId").value(77))
                .andExpect(jsonPath("$.description").value("Updated block"))
                .andExpect(jsonPath("$.plantDate").value("2026-06-01T09:30:00"));

        mockMvc.perform(delete("/api/plantations/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Plantation deleted successfully"));

        mockMvc.perform(get("/api/plantations/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Plantation not found with id: " + id));
    }

    @Test
    void validationAndMissingResourcePathsReturnErrors() throws Exception {
        mockMvc.perform(post("/api/plantations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "location": "",
                                  "area": -1
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/plantations/{id}", 404)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Missing",
                                  "location": "Nowhere",
                                  "area": 1
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Plantation not found with id: 404"));

        mockMvc.perform(delete("/api/plantations/{id}", 404))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Plantation not found with id: 404"));
    }

    @Test
    void healthEndpointReportsServiceName() throws Exception {
        mockMvc.perform(get("/api/plantations/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("mysawit-plantation-service"));
    }
}
