package com.industriaplus.backend.preventive;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.*;

public interface PreventivePlanRepository extends JpaRepository<PreventivePlan, Long> {
    Optional<PreventivePlan> findByEquipmentId(Long equipmentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PreventivePlan p where p.equipment.id = :equipmentId")
    Optional<PreventivePlan> findByEquipmentIdForUpdate(@Param("equipmentId") Long equipmentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PreventivePlan p where p.id = :id")
    Optional<PreventivePlan> findByIdForUpdate(@Param("id") Long id);

    @Query("select p.id from PreventivePlan p where p.enabled = true and p.nextDueDate <= :today")
    List<Long> findDueIds(@Param("today") LocalDate today);
}
