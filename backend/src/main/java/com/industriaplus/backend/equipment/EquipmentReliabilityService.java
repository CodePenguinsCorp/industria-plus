package com.industriaplus.backend.equipment;

import com.industriaplus.backend.auth.AccessService;
import com.industriaplus.backend.error.BusinessException;
import com.industriaplus.backend.maintenancerequest.MaintenanceRequestRepository;
import com.industriaplus.backend.maintenancerequest.MaintenanceType;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentReliabilityService {
    private final EquipmentService equipment;
    private final EquipmentRepository equipments;
    private final MaintenanceRequestRepository requests;
    private final AccessService access;

    public ReliabilityResponse save(Long id, ReliabilityInput input) {
        access.requireManager();
        if (input.endDate().isBefore(input.startDate())
                || input.endDate().isAfter(LocalDate.now(ZoneId.of("America/Sao_Paulo")))) {
            throw invalid("Informe um período válido, sem datas futuras.");
        }
        long calendarHours = (ChronoUnit.DAYS.between(input.startDate(), input.endDate()) + 1) * 24;
        if (input.operatingHours().compareTo(BigDecimal.valueOf(calendarHours)) > 0) {
            throw invalid("As horas de operação não podem exceder a duração do período.");
        }
        Equipment item = equipment.getRequired(id);
        item.measureOperation(input.startDate(), input.endDate(), input.operatingHours());
        equipments.saveAndFlush(item);
        return response(item);
    }

    @Transactional(readOnly = true)
    public ReliabilityResponse get(Long id) {
        return response(equipment.getRequired(id));
    }

    private ReliabilityResponse response(Equipment item) {
        if (item.getOperatingHours() == null) {
            return new ReliabilityResponse(null, null, null, 0, null, "INSUFFICIENT_DATA");
        }
        long failures = requests.countFailures(item.getId(), MaintenanceType.CORRECTIVE,
            item.getObservationStart().atStartOfDay(), item.getObservationEnd().plusDays(1).atStartOfDay());
        BigDecimal mtbf = failures == 0 ? null : item.getOperatingHours()
            .divide(BigDecimal.valueOf(failures), 2, RoundingMode.HALF_UP);
        return new ReliabilityResponse(item.getObservationStart(), item.getObservationEnd(),
            item.getOperatingHours(), failures, mtbf, failures == 0 ? "NO_FAILURES" : "CALCULATED");
    }

    private BusinessException invalid(String message) {
        return new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_OBSERVATION", message);
    }

    public record ReliabilityInput(@NotNull LocalDate startDate, @NotNull LocalDate endDate,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal operatingHours) {}

    public record ReliabilityResponse(LocalDate startDate, LocalDate endDate, BigDecimal operatingHours,
        long failureCount, BigDecimal mtbfHours, String state) {}
}
