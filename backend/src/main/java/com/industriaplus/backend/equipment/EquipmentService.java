package com.industriaplus.backend.equipment;

import com.industriaplus.backend.error.BusinessException;
import com.industriaplus.backend.sector.Sector;
import com.industriaplus.backend.sector.SectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final SectorService sectorService;

    public EquipmentResponse create(EquipmentRequest request) {
        String assetTag = request.assetTag().trim();
        if (equipmentRepository.existsByAssetTagIgnoreCase(assetTag)) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "DUPLICATE_ASSET_TAG",
                "Já existe um equipamento com o patrimônio informado."
            );
        }

        Sector sector = sectorService.getRequired(request.sectorId());
        Equipment equipment = new Equipment(
            assetTag,
            request.name().trim(),
            trimToNull(request.description()),
            sector
        );
        return EquipmentResponse.from(equipmentRepository.save(equipment));
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> list() {
        return equipmentRepository.findAllByOrderByNameAsc().stream()
            .map(EquipmentResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public Equipment getRequired(Long id) {
        return equipmentRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "EQUIPMENT_NOT_FOUND",
                "Equipamento não encontrado."
            ));
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
