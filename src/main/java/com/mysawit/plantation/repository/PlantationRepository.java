package com.mysawit.plantation.repository;

import com.mysawit.plantation.model.Plantation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantationRepository extends JpaRepository<Plantation, Long> {
    List<Plantation> findByOwnerId(Long ownerId);
    List<Plantation> findByLocation(String location);
}
