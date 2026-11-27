package com.industriaplus.backend.technician;

import com.industriaplus.backend.auth.AccessService;
import com.industriaplus.backend.auth.AppUser;
import com.industriaplus.backend.auth.Role;
import com.industriaplus.backend.auth.UserRepository;
import com.industriaplus.backend.error.BusinessException;
import com.industriaplus.backend.maintenancerequest.MaintenanceRequestRepository;
import com.industriaplus.backend.maintenancerequest.MaintenanceRequestStatus;
import com.industriaplus.backend.maintenancerequest.Urgency;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Técnicos são usuários com o perfil {@link Role#TECHNICIAN}. O cadastro é feito em Usuários;
 * aqui ficam a consulta da equipe, o vínculo com o gestor e a exclusão.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TechnicianService {

    private final UserRepository users;
    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final AccessService access;

    @Transactional(readOnly = true)
    public List<TechnicianResponse> list() {
        Map<Long, Long> workloads =
                maintenanceRequestRepository
                        .findTechnicianWorkloads(Urgency.HIGH, MaintenanceRequestStatus.CLOSED)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        TechnicianWorkload::technicianId,
                                        TechnicianWorkload::highUrgencyOpenRequests));

        return users.findByRoleOrderByNameAsc(Role.TECHNICIAN).stream()
                .map(
                        technician ->
                                TechnicianResponse.from(
                                        technician,
                                        workloads.getOrDefault(technician.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ManagerOption> managers() {
        return users.findByRoleOrderByNameAsc(Role.MANAGER).stream()
                .map(ManagerOption::from)
                .toList();
    }

    /** Bloqueia o técnico para validar a RN-001 sem condição de corrida. */
    public AppUser getRequiredForUpdate(Long id) {
        return users.findByIdAndRoleForUpdate(id, Role.TECHNICIAN)
                .orElseThrow(this::technicianNotFound);
    }

    public TechnicianResponse updateManager(Long technicianId, TechnicianManagerRequest input) {
        access.requireAdmin();
        AppUser technician = getRequiredForUpdate(technicianId);
        AppUser manager =
                users.findByIdAndRole(input.managerId(), Role.MANAGER)
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                HttpStatus.BAD_REQUEST,
                                                "INVALID_MANAGER_LINK",
                                                "Selecione um gestor válido."));
        technician.assignManager(manager);
        users.saveAndFlush(technician);
        long workload =
                maintenanceRequestRepository.countActiveByTechnicianAndUrgency(
                        technician.getId(), Urgency.HIGH, MaintenanceRequestStatus.CLOSED);
        return TechnicianResponse.from(technician, workload);
    }

    public void delete(Long id) {
        access.requireAdmin();
        AppUser technician =
                users.findByIdAndRole(id, Role.TECHNICIAN).orElseThrow(this::technicianNotFound);
        if (maintenanceRequestRepository.existsByTechnicianId(id)) {
            throw technicianInUse();
        }
        try {
            users.delete(technician);
            users.flush();
        } catch (DataIntegrityViolationException exception) {
            throw technicianInUse();
        }
    }

    private BusinessException technicianInUse() {
        return new BusinessException(
                HttpStatus.CONFLICT,
                "TECHNICIAN_IN_USE",
                "O técnico não pode ser excluído enquanto possuir chamados vinculados.");
    }

    private BusinessException technicianNotFound() {
        return new BusinessException(
                HttpStatus.NOT_FOUND, "TECHNICIAN_NOT_FOUND", "Técnico não encontrado.");
    }
}
