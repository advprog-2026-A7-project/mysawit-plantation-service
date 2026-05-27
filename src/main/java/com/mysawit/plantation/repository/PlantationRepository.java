package com.mysawit.plantation.repository;

import com.mysawit.plantation.model.Plantation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantationRepository extends JpaRepository<Plantation, Long> {

    @Override
    @EntityGraph(attributePaths = {"coordinates", "supirIds"})
    List<Plantation> findAll();

    @Override
    @EntityGraph(attributePaths = {"coordinates", "supirIds"})
    Optional<Plantation> findById(Long id);

    Optional<Plantation> findByCode(String code);

    @EntityGraph(attributePaths = {"coordinates", "supirIds"})
    List<Plantation> findByOwnerId(String ownerId);

    @EntityGraph(attributePaths = {"coordinates", "supirIds"})
    List<Plantation> findByNameContainingIgnoreCase(String name);

    @EntityGraph(attributePaths = {"coordinates", "supirIds"})
    List<Plantation> findByCodeContainingIgnoreCase(String code);

    @EntityGraph(attributePaths = {"coordinates", "supirIds"})
    List<Plantation> findByNameContainingIgnoreCaseAndCodeContainingIgnoreCase(String name, String code);

    Optional<Plantation> findByMandorId(String mandorId);

    @EntityGraph(attributePaths = {"coordinates", "supirIds"})
    List<Plantation> findAllByMandorId(String mandorId);

    @EntityGraph(attributePaths = {"coordinates", "supirIds"})
    List<Plantation> findBySupirIdsContaining(String supirId);

}
