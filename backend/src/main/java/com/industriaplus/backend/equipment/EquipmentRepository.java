package com.industriaplus.backend.equipment;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    boolean existsByAssetTagIgnoreCase(String assetTag);

    @EntityGraph(attributePaths = "sector")
    List<Equipment> findAllByOrderByNameAsc();
}
