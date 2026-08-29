package com.industriaplus.backend;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MaintenanceRequestApiIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    void shouldCreateListFilterAndPrioritizeMaintenanceRequests() throws Exception {
        long sectorId = createSector("Produção");
        long equipmentId = createEquipment(sectorId, "PRD-001");
        createMaintenanceRequest(equipmentId, sectorId, "LOW");
        long firstHighRequestId = createMaintenanceRequest(equipmentId, sectorId, "HIGH");
        long mediumRequestId = createMaintenanceRequest(equipmentId, sectorId, "MEDIUM");
        long newestHighRequestId = createMaintenanceRequest(equipmentId, sectorId, "HIGH");

        mockMvc.perform(get("/api/maintenance-requests").contextPath("/api"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(4))
            .andExpect(jsonPath("$[0].urgency").value("HIGH"))
            .andExpect(jsonPath("$[0].id").value(newestHighRequestId))
            .andExpect(jsonPath("$[1].urgency").value("HIGH"))
            .andExpect(jsonPath("$[1].id").value(firstHighRequestId))
            .andExpect(jsonPath("$[2].urgency").value("MEDIUM"))
            .andExpect(jsonPath("$[3].urgency").value("LOW"))
            .andExpect(jsonPath("$[0].status").value("OPEN"))
            .andExpect(jsonPath("$[0].technicianId").doesNotExist())
            .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
            .andExpect(jsonPath("$[0].updatedAt").isNotEmpty());

        mockMvc.perform(get("/api/maintenance-requests")
                .contextPath("/api")
                .queryParam("urgency", "LOW"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].urgency").value("LOW"));

        mockMvc.perform(get("/api/maintenance-requests")
                .contextPath("/api")
                .queryParam("status", "OPEN")
                .queryParam("urgency", "HIGH"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].urgency").value("HIGH"));

        assertEquals(200, updateStatus(mediumRequestId, "IN_PROGRESS").getResponse().getStatus());
        mockMvc.perform(get("/api/maintenance-requests")
                .contextPath("/api")
                .queryParam("status", "IN_PROGRESS"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(mediumRequestId));

        mockMvc.perform(get("/api/maintenance-requests")
                .contextPath("/api")
                .queryParam("urgency", "URGENT"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    void shouldRejectMismatchedSectorAndValidateTrimmedRequestText() throws Exception {
        long equipmentSectorId = createSector("Caldeiraria");
        long otherSectorId = createSector("Expedição");
        long equipmentId = createEquipment(equipmentSectorId, "CAL-001");

        mockMvc.perform(post("/api/maintenance-requests")
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "title", "Falha no equipamento",
                    "description", "Descrição suficiente para abertura",
                    "equipmentId", equipmentId,
                    "sectorId", otherSectorId,
                    "type", "CORRECTIVE",
                    "urgency", "HIGH"
                ))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("EQUIPMENT_SECTOR_MISMATCH"));

        mockMvc.perform(post("/api/maintenance-requests")
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "title", " x ",
                    "description", " curta ",
                    "equipmentId", equipmentId,
                    "sectorId", equipmentSectorId,
                    "type", "CORRECTIVE",
                    "urgency", "HIGH"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fieldErrors.title").exists())
            .andExpect(jsonPath("$.fieldErrors.description").exists());
    }

    @Test
    void shouldEnforceAllowedStatusTransitionsAndClosedTerminalState() throws Exception {
        long sectorId = createSector("Pintura");
        long equipmentId = createEquipment(sectorId, "PIN-001");
        long technicianId = createTechnician("status@example.com");
        long requestId = createMaintenanceRequest(equipmentId, sectorId, "MEDIUM");

        assertEquals(200, updateStatus(requestId, "IN_PROGRESS").getResponse().getStatus());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                "/api/maintenance-requests/{id}/status",
                requestId
            )
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("status", "OPEN"))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

        assertEquals(200, updateStatus(requestId, "CLOSED").getResponse().getStatus());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                "/api/maintenance-requests/{id}/assignment",
                requestId
            )
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("technicianId", technicianId))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("MAINTENANCE_REQUEST_CLOSED"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                "/api/maintenance-requests/{id}/status",
                requestId
            )
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("status", "CLOSED"))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("MAINTENANCE_REQUEST_CLOSED"));

        long directCloseRequestId = createMaintenanceRequest(equipmentId, sectorId, "LOW");
        JsonNode directCloseBody = body(updateStatus(directCloseRequestId, "CLOSED"));
        assertEquals("CLOSED", directCloseBody.get("status").asText());
    }

    @Test
    void shouldReturnNotFoundAndMalformedEnumErrorsInStandardEnvelope() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                "/api/maintenance-requests/{id}/status",
                99999
            )
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("status", "CLOSED"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("MAINTENANCE_REQUEST_NOT_FOUND"))
            .andExpect(jsonPath("$.timestamp").isNotEmpty())
            .andExpect(jsonPath("$.path").value("/api/maintenance-requests/99999/status"));

        long sectorId = createSector("Utilidades");
        long equipmentId = createEquipment(sectorId, "UTL-001");
        mockMvc.perform(post("/api/maintenance-requests")
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "title", "Falha de utilidade",
                    "description", "Descrição suficiente da falha",
                    "equipmentId", equipmentId,
                    "sectorId", sectorId,
                    "type", "UNKNOWN",
                    "urgency", "HIGH"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }
}
