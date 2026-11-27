package com.industriaplus.backend.history;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartRepository extends JpaRepository<ReplacedPart, Long> {
    List<ReplacedPart> findByHistoryId(Long id);
}
