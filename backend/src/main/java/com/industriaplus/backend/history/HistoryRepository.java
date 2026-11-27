package com.industriaplus.backend.history;

import org.springframework.data.jpa.repository.*;

import java.util.List;

public interface HistoryRepository extends JpaRepository<HistoryEntry, Long> {
    @EntityGraph(attributePaths = "request")
    List<HistoryEntry> findByRequestIdOrderByCreatedAtDescIdDesc(Long id);

    @EntityGraph(attributePaths = "request")
    List<HistoryEntry> findByRequestEquipmentIdOrderByCreatedAtDescIdDesc(Long id);
}
