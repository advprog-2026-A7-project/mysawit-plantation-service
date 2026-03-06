package com.mysawit.plantation.integration;

import com.mysawit.plantation.dto.CreatePlantationRequest;
import com.mysawit.plantation.dto.PlantationResponse;
import com.mysawit.plantation.dto.UpdatePlantationRequest;
import com.mysawit.plantation.repository.PlantationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class PlantationIntegrationTest {

    private static final String POSTGRES_ENABLED_PROPERTY = "integration.postgres.enabled";
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PlantationRepository plantationRepository;

    private String baseUrl;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (isPostgresEnabled()) {
            if (!postgres.isRunning()) {
                postgres.start();
            }
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
            registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
            registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        }
    }

    private static boolean isPostgresEnabled() {
        return Boolean.parseBoolean(System.getProperty(POSTGRES_ENABLED_PROPERTY, "false"));
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/plantations";
        plantationRepository.deleteAll(); // Clean up before each test
    }

    @AfterEach
    void tearDown() {
        plantationRepository.deleteAll(); // Clean up after each test
    }

    @Test
    void testCreatePlantation() {
        CreatePlantationRequest request = new CreatePlantationRequest();
        request.setName("Integration Test Plantation");
        request.setLocation("Test Location");
        request.setArea(250.0);
        request.setOwnerId("Owner-123");
        request.setDescription("Integration Description");
        request.setPlantDate(LocalDateTime.now().minusDays(10));

        ResponseEntity<PlantationResponse> response = restTemplate.postForEntity(
                baseUrl, request, PlantationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getCode()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Integration Test Plantation");

        // Verify it was stored in the database
        assertThat(plantationRepository.findAll()).hasSize(1);
    }

    @Test
    void testGetPlantationById() {
        PlantationResponse created = createTestPlantation("Test Get", "Location 1", "Owner-100");

        ResponseEntity<PlantationResponse> response = restTemplate.getForEntity(
                baseUrl + "/" + created.getId(), PlantationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Test Get");
        assertThat(response.getBody().getOwnerId()).isEqualTo("Owner-100");
    }

    @Test
    void testGetPlantationByIdNotFound() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/99999", Map.class); // Explicit Map type for error body

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("error")).isEqualTo("Plantation not found with id: 99999");
    }

    @Test
    void testUpdatePlantation() {
        PlantationResponse original = createTestPlantation("Original Name", "Original Location", "Owner-200");

        UpdatePlantationRequest updateRequest = new UpdatePlantationRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setLocation("Updated Location");
        updateRequest.setArea(300.5);
        updateRequest.setDescription("Updated Description");
        updateRequest.setPlantDate(LocalDateTime.now().minusDays(5));

        HttpEntity<UpdatePlantationRequest> requestEntity = new HttpEntity<>(updateRequest);

        ResponseEntity<PlantationResponse> response = restTemplate.exchange(
                baseUrl + "/" + original.getId(),
                HttpMethod.PUT,
                requestEntity,
                PlantationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Updated Name");
        assertThat(response.getBody().getLocation()).isEqualTo("Updated Location");
        assertThat(response.getBody().getArea()).isEqualTo(300.5);
        assertThat(response.getBody().getCode()).isEqualTo(original.getCode()); // Code shouldn't change
    }

    @Test
    void testUpdatePlantationNotFound() {
        UpdatePlantationRequest updateRequest = new UpdatePlantationRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setLocation("Updated Location");
        updateRequest.setArea(300.5);
        updateRequest.setDescription("Updated Description");
        updateRequest.setPlantDate(LocalDateTime.now().minusDays(5));

        HttpEntity<UpdatePlantationRequest> requestEntity = new HttpEntity<>(updateRequest);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/99999",
                HttpMethod.PUT,
                requestEntity,
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsKey("error");
    }

    @Test
    void testDeletePlantation() {
        PlantationResponse created = createTestPlantation("To Delete", "Location X", "Owner-300");

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + created.getId(),
                HttpMethod.DELETE,
                null,
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(plantationRepository.findById(created.getId())).isEmpty();
    }

    @Test
    void testDeletePlantationNotFound() {
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/99999",
                HttpMethod.DELETE,
                null,
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsKey("error");
    }

    @Test
    void testGetPlantationsByOwner() {
        createTestPlantation("Owner A Plant 1", "Location 1", "Owner-A");
        createTestPlantation("Owner A Plant 2", "Location 2", "Owner-A");
        createTestPlantation("Owner B Plant 1", "Location 3", "Owner-B");

        ResponseEntity<List<PlantationResponse>> response = restTemplate.exchange(
                baseUrl + "/owner/Owner-A",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PlantationResponse>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).extracting(PlantationResponse::getOwnerId).containsOnly("Owner-A");
    }

    @Test
    void testGetAllPlantations() {
        createTestPlantation("Global 1", "Location G1", "Owner-1");
        createTestPlantation("Global 2", "Location G2", "Owner-2");

        ResponseEntity<List<PlantationResponse>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PlantationResponse>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    // Helper method to create plantations directly for subsequent testing
    private PlantationResponse createTestPlantation(String name, String location, String ownerId) {
        CreatePlantationRequest request = new CreatePlantationRequest();
        request.setName(name);
        request.setLocation(location);
        request.setArea(100.0);
        request.setOwnerId(ownerId);
        request.setDescription("Test Setup Data");
        request.setPlantDate(LocalDateTime.now().minusMonths(1));

        return restTemplate.postForObject(baseUrl, request, PlantationResponse.class);
    }
}
