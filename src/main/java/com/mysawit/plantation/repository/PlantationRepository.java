package com.mysawit.plantation.repository;

import com.mysawit.plantation.model.Plantation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantationRepository extends JpaRepository<Plantation, Long> {

    Optional<Plantation> findByCode(String code);

    List<Plantation> findByOwnerId(String ownerId);

    Optional<Plantation> findByMandorId(String mandorId);

}
