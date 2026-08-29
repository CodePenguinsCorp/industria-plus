package com.industriaplus.backend.maintenancerequest;

import com.industriaplus.backend.equipment.Equipment;
import com.industriaplus.backend.equipment.EquipmentService;
import com.industriaplus.backend.error.BusinessException;
import com.industriaplus.backend.sector.Sector;
import com.industriaplus.backend.sector.SectorService;
import com.industriaplus.backend.technician.Technician;
import com.industriaplus.backend.technician.TechnicianService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceRequestService {

    private static final long MAX_HIGH_URGENCY_OPEN_REQUESTS = 2;

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final EquipmentService equipmentService;
    private final SectorService sectorService;
    private final TechnicianService technicianService;

    public MaintenanceRequestResponse create(MaintenanceRequestCreateRequest request) {
        Equipment equipment = equipmentService.getRequired(request.equipmentId());
        Sector sector = sectorService.getRequired(request.sectorId());

        if (!Objects.equals(equipment.getSector().getId(), sector.getId())) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "EQUIPMENT_SECTOR_MISMATCH",
                "O equipamento informado não pertence ao setor selecionado."
            );
        }

        MaintenanceRequest maintenanceRequest = new MaintenanceRequest(
            request.title().trim(),
            request.description().trim(),
            equipment,
            sector,
            request.type(),
            request.urgency()
        );
        return MaintenanceRequestResponse.from(
            maintenanceRequestRepository.save(maintenanceRequest)
        );
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRequestResponse> list(
        MaintenanceRequestStatus status,
        Urgency urgency
    ) {
        return maintenanceRequestRepository.findFiltered(status, urgency).stream()
            .sorted(
                Comparator.comparingInt(
                        (MaintenanceRequest request) -> request.getUrgency().priority()
                    )
                    .reversed()
                    .thenComparing(
                        MaintenanceRequest::getCreatedAt,
                        Comparator.reverseOrder()
                    )
                    .thenComparing(
                        MaintenanceRequest::getId,
                        Comparator.reverseOrder()
                    )
            )
            .map(MaintenanceRequestResponse::from)
            .toList();
    }

    public MaintenanceRequestResponse assign(
        Long requestId,
        MaintenanceRequestAssignmentRequest assignment
    ) {
        MaintenanceRequest request = getRequiredForUpdate(requestId);
        if (request.getStatus() == MaintenanceRequestStatus.CLOSED) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "MAINTENANCE_REQUEST_CLOSED",
                "Um chamado encerrado não pode ser atribuído ou reatribuído."
            );
        }

        Technician technician = technicianService.getRequiredForUpdate(
            assignment.technicianId()
        );
        if (request.getTechnician() != null
            && Objects.equals(request.getTechnician().getId(), technician.getId())) {
            return MaintenanceRequestResponse.from(request);
        }

        validateHighUrgencyLimit(request, technician);
        request.assignTo(technician);
        return MaintenanceRequestResponse.from(
            maintenanceRequestRepository.saveAndFlush(request)
        );
    }

    public MaintenanceRequestResponse updateStatus(
        Long requestId,
        MaintenanceRequestStatusRequest statusRequest
    ) {
        MaintenanceRequest request = getRequiredForUpdate(requestId);
        MaintenanceRequestStatus targetStatus = statusRequest.status();
        if (request.getStatus() == MaintenanceRequestStatus.CLOSED) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "MAINTENANCE_REQUEST_CLOSED",
                "Um chamado encerrado não pode ser alterado."
            );
        }
        if (!request.getStatus().canTransitionTo(targetStatus)) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "INVALID_STATUS_TRANSITION",
                "A transição de status de %s para %s não é permitida."
                    .formatted(request.getStatus(), targetStatus)
            );
        }

        request.transitionTo(targetStatus);
        return MaintenanceRequestResponse.from(
            maintenanceRequestRepository.saveAndFlush(request)
        );
    }

    private void validateHighUrgencyLimit(
        MaintenanceRequest request,
        Technician technician
    ) {
        if (request.getUrgency() != Urgency.HIGH) {
            return;
        }

        long currentHighUrgencyRequests = maintenanceRequestRepository
            .countActiveByTechnicianAndUrgency(
                technician.getId(),
                Urgency.HIGH,
                MaintenanceRequestStatus.CLOSED
            );
        if (currentHighUrgencyRequests >= MAX_HIGH_URGENCY_OPEN_REQUESTS) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "HIGH_URGENCY_LIMIT",
                "O técnico não pode ter mais de 2 chamados de urgência Alta abertos ao mesmo tempo."
            );
        }
    }

    private MaintenanceRequest getRequiredForUpdate(Long id) {
        return maintenanceRequestRepository.findByIdForUpdate(id)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "MAINTENANCE_REQUEST_NOT_FOUND",
                "Chamado de manutenção não encontrado."
            ));
    }
}
