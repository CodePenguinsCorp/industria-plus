package com.industriaplus.backend.sector;

import com.industriaplus.backend.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SectorService {

    private final SectorRepository sectorRepository;

    public SectorResponse create(SectorRequest request) {
        Sector sector = new Sector(
            request.name().trim(),
            trimToNull(request.description())
        );
        return SectorResponse.from(sectorRepository.save(sector));
    }

    @Transactional(readOnly = true)
    public List<SectorResponse> list() {
        return sectorRepository.findAllByOrderByNameAsc().stream()
            .map(SectorResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public Sector getRequired(Long id) {
        return sectorRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "SECTOR_NOT_FOUND",
                "Setor não encontrado."
            ));
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
