package com.industriaplus.backend.maintenancerequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/maintenance-requests")
@RequiredArgsConstructor
public class MaintenanceRequestController {

    private final MaintenanceRequestService maintenanceRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceRequestResponse create(
        @Valid @RequestBody MaintenanceRequestCreateRequest request
    ) {
        return maintenanceRequestService.create(request);
    }

    @GetMapping
    public List<MaintenanceRequestResponse> list(
        @RequestParam(required = false) MaintenanceRequestStatus status,
        @RequestParam(required = false) Urgency urgency
    ) {
        return maintenanceRequestService.list(status, urgency);
    }

    @PatchMapping("/{id}/assignment")
    public MaintenanceRequestResponse assign(
        @PathVariable Long id,
        @Valid @RequestBody MaintenanceRequestAssignmentRequest request
    ) {
        return maintenanceRequestService.assign(id, request);
    }

    @PatchMapping("/{id}/status")
    public MaintenanceRequestResponse updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody MaintenanceRequestStatusRequest request
    ) {
        return maintenanceRequestService.updateStatus(id, request);
    }
}
