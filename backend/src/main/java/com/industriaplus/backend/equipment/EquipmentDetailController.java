package com.industriaplus.backend.equipment;

import com.industriaplus.backend.history.*;
import com.industriaplus.backend.maintenancerequest.*;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/equipments")
@RequiredArgsConstructor
public class EquipmentDetailController {
    private final EquipmentService equipment;
    private final MaintenanceRequestRepository requests;
    private final HistoryService history;

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public EquipmentDetail detail(@PathVariable Long id) {
        var item = equipment.getRequired(id);
        return new EquipmentDetail(
                EquipmentResponse.from(item),
                requests.findByEquipmentIdOrderByCreatedAtDescIdDesc(id).stream()
                        .map(MaintenanceRequestResponse::from)
                        .toList(),
                history.equipmentHistory(id));
    }

    public record EquipmentDetail(
            EquipmentResponse equipment,
            List<MaintenanceRequestResponse> requests,
            List<HistoryResponse> history) {}
}
