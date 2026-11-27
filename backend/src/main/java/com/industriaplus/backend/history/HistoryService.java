package com.industriaplus.backend.history;

import com.industriaplus.backend.auth.AccessService;
import com.industriaplus.backend.error.BusinessException;
import com.industriaplus.backend.maintenancerequest.*;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HistoryService {
    private final HistoryRepository history;
    private final PartRepository parts;
    private final MaintenanceRequestRepository requests;
    private final AccessService access;

    public void record(
            MaintenanceRequest request,
            String type,
            String description,
            String previousStatus,
            Long previousTechId,
            String previousTechName) {
        history.save(
                new HistoryEntry(
                        request,
                        type,
                        access.actor(),
                        description,
                        previousStatus,
                        previousTechId,
                        previousTechName));
    }

    @Transactional(readOnly = true)
    public List<HistoryResponse> requestHistory(Long id) {
        if (!requests.existsById(id))
            throw new BusinessException(
                    HttpStatus.NOT_FOUND,
                    "MAINTENANCE_REQUEST_NOT_FOUND",
                    "Chamado não encontrado.");
        return history.findByRequestIdOrderByCreatedAtDescIdDesc(id).stream()
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HistoryResponse> equipmentHistory(Long id) {
        return history.findByRequestEquipmentIdOrderByCreatedAtDescIdDesc(id).stream()
                .map(this::response)
                .toList();
    }

    public HistoryResponse intervene(Long id, InterventionRequest input) {
        MaintenanceRequest request =
                requests.findByIdForUpdate(id)
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                HttpStatus.NOT_FOUND,
                                                "MAINTENANCE_REQUEST_NOT_FOUND",
                                                "Chamado não encontrado."));
        access.requireAttendance(request);
        if (request.getStatus() == MaintenanceRequestStatus.CLOSED)
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "MAINTENANCE_REQUEST_CLOSED",
                    "Não é possível registrar serviço em chamado encerrado.");
        if (input.description().trim().length() < 3)
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_DESCRIPTION",
                    "Descreva o serviço realizado com ao menos 3 caracteres.");
        var entry =
                history.saveAndFlush(
                        new HistoryEntry(
                                request,
                                "INTERVENTION",
                                access.actor(),
                                input.description().trim(),
                                request.getStatus().name(),
                                request.getTechnician() == null
                                        ? null
                                        : request.getTechnician().getId(),
                                request.getTechnician() == null
                                        ? null
                                        : request.getTechnician().getName()));
        if (input.parts() != null)
            for (var part : input.parts())
                parts.save(new ReplacedPart(entry, part.name().trim(), part.quantity()));
        parts.flush();
        return response(entry);
    }

    private HistoryResponse response(HistoryEntry entry) {
        return HistoryResponse.from(entry, parts.findByHistoryId(entry.getId()));
    }
}
