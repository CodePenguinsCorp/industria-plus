package com.industriaplus.backend.technician;

import com.industriaplus.backend.error.BusinessException;
import com.industriaplus.backend.maintenancerequest.MaintenanceRequestRepository;
import com.industriaplus.backend.maintenancerequest.MaintenanceRequestStatus;
import com.industriaplus.backend.maintenancerequest.Urgency;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TechnicianService {

    private final TechnicianRepository technicianRepository;
    private final MaintenanceRequestRepository maintenanceRequestRepository;

    public TechnicianResponse create(TechnicianRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (technicianRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "DUPLICATE_TECHNICIAN_EMAIL",
                "Já existe um técnico com o e-mail informado."
            );
        }

        Technician technician = new Technician(
            request.name().trim(),
            email,
            trimToNull(request.specialty())
        );
        return TechnicianResponse.from(technicianRepository.save(technician), 0);
    }

    @Transactional(readOnly = true)
    public List<TechnicianResponse> list() {
        Map<Long, Long> workloads = maintenanceRequestRepository.findTechnicianWorkloads(
                Urgency.HIGH,
                MaintenanceRequestStatus.CLOSED
            ).stream()
            .collect(Collectors.toMap(
                TechnicianWorkload::technicianId,
                TechnicianWorkload::highUrgencyOpenRequests
            ));

        return technicianRepository.findAllByOrderByNameAsc().stream()
            .map(technician -> TechnicianResponse.from(
                technician,
                workloads.getOrDefault(technician.getId(), 0L)
            ))
            .toList();
    }

    public Technician getRequiredForUpdate(Long id) {
        return technicianRepository.findByIdForUpdate(id)
            .orElseThrow(() -> technicianNotFound());
    }

    private BusinessException technicianNotFound() {
        return new BusinessException(
            HttpStatus.NOT_FOUND,
            "TECHNICIAN_NOT_FOUND",
            "Técnico não encontrado."
        );
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
