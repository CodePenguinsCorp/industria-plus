package com.industriaplus.backend.sector;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectorRepository extends JpaRepository<Sector, Long> {

    List<Sector> findAllByOrderByNameAsc();
}
