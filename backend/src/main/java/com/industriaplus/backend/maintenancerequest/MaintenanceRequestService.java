package com.industriaplus.backend.maintenancerequest;

import com.industriaplus.backend.equipment.Equipment;
import com.industriaplus.backend.equipment.EquipmentService;
import com.industriaplus.backend.error.BusinessException;
import com.industriaplus.backend.sector.Sector;
import com.industriaplus.backend.sector.SectorService;
import com.industriaplus.backend.auth.AppUser;
import com.industriaplus.backend.technician.TechnicianService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceRequestService {

    private static final long MAX_HIGH_URGENCY_OPEN_REQUESTS = 2;

    private final com.industriaplus.backend.auth.AccessService access;
    private final com.industriaplus.backend.history.HistoryService history;
    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final EquipmentService equipmentService;
    private final SectorService sectorService;
    private final TechnicianService technicianService;

    public MaintenanceRequestResponse create(MaintenanceRequestCreateRequest request) {
        access.requireManager();
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
        maintenanceRequestRepository.saveAndFlush(maintenanceRequest);
        history.record(maintenanceRequest, "CREATED", "Chamado aberto.", null, null, null);
        return MaintenanceRequestResponse.from(maintenanceRequest);
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

    @Transactional(readOnly = true)
    public MaintenanceRequestResponse detail(Long id) {
        return MaintenanceRequestResponse.from(maintenanceRequestRepository.findById(id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "MAINTENANCE_REQUEST_NOT_FOUND", "Chamado não encontrado.")));
    }

    // Após aguardar o lock do técnico, a contagem deve enxergar a atribuição já confirmada.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MaintenanceRequestResponse assign(
        Long requestId,
        MaintenanceRequestAssignmentRequest assignment
    ) {
        access.requireManager();
        MaintenanceRequest request = getRequiredForUpdate(requestId);
        if (request.getStatus() == MaintenanceRequestStatus.CLOSED) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "MAINTENANCE_REQUEST_CLOSED",
                "Um chamado encerrado não pode ser atribuído ou reatribuído."
            );
        }

        AppUser technician = technicianService.getRequiredForUpdate(
            assignment.technicianId()
        );
        if (request.getTechnician() != null
            && Objects.equals(request.getTechnician().getId(), technician.getId())) {
            return MaintenanceRequestResponse.from(request);
        }

        validateHighUrgencyLimit(request, technician);
        Long previousId = request.getTechnician() == null ? null : request.getTechnician().getId();
        String previousName = request.getTechnician() == null ? null : request.getTechnician().getName();
        request.assignTo(technician);
        history.record(request, "ASSIGNMENT", "Responsável alterado.", request.getStatus().name(), previousId, previousName);
        return MaintenanceRequestResponse.from(
            maintenanceRequestRepository.saveAndFlush(request)
        );
    }

    public MaintenanceRequestResponse updateStatus(
        Long requestId,
        MaintenanceRequestStatusRequest statusRequest
    ) {
        MaintenanceRequest request = getRequiredForUpdate(requestId);
        access.requireAttendance(request);
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

        String previousStatus = request.getStatus().name();
        request.transitionTo(targetStatus);
        history.record(request, "STATUS", "Status alterado.", previousStatus,
            request.getTechnician() == null ? null : request.getTechnician().getId(),
            request.getTechnician() == null ? null : request.getTechnician().getName());
        return MaintenanceRequestResponse.from(
            maintenanceRequestRepository.saveAndFlush(request)
        );
    }

    private void validateHighUrgencyLimit(
        MaintenanceRequest request,
        AppUser technician
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
