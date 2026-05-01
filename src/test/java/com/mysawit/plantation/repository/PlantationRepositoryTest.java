package com.mysawit.plantation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mysawit.plantation.model.Plantation;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import com.mysawit.plantation.config.JpaAuditingConfig;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
public class PlantationRepositoryTest {

    @Autowired
    private PlantationRepository plantationRepository;

    @Test
    public void testSavePlantationSuccessfully() {
        Plantation plantation = new Plantation("P-001", "North Estate", "Sumatra", 1500.5);
        plantation.setOwnerId("user-123");
        plantation.setDescription("Main northern plantation block");

        Plantation saved = plantationRepository.save(plantation);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("P-001");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    public void testUniqueConstraintOnCode() {
        Plantation p1 = new Plantation("P-002", "South Estate", "Java", 500.0);
        plantationRepository.saveAndFlush(p1);

        Plantation p2 = new Plantation("P-002", "West Estate", "Kalimantan", 800.0);
        
        assertThrows(DataIntegrityViolationException.class, () -> {
            plantationRepository.saveAndFlush(p2);
        });
    }

    @Test
    @Sql(scripts = "/dataset/plantations-test-data.sql")
    public void testFindByCode() {
        Optional<Plantation> result = plantationRepository.findByCode("TEST-001");
        
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Estate Alpha");
    }

    @Test
    @Sql(scripts = "/dataset/plantations-test-data.sql")
    public void testFindByCodeNotFound() {
        Optional<Plantation> result = plantationRepository.findByCode("NON-EXISTENT");
        
        assertThat(result).isEmpty();
    }

    @Test
    @Sql(scripts = "/dataset/plantations-test-data.sql")
    public void testFindByOwnerId() {
        List<Plantation> plantations = plantationRepository.findByOwnerId("owner-auth-999");
        
        assertThat(plantations).hasSize(2);
        assertThat(plantations).extracting(Plantation::getCode)
                .containsExactlyInAnyOrder("TEST-001", "TEST-002");
    }

    @Test
    @Sql(scripts = "/dataset/plantations-test-data.sql")
    public void testFindByOwnerIdEmpty() {
        List<Plantation> plantations = plantationRepository.findByOwnerId("unknown-owner");
        
        assertThat(plantations).isEmpty();
    }
}
