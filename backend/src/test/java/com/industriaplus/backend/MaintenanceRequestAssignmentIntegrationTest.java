package com.industriaplus.backend;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MaintenanceRequestAssignmentIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    void shouldCoverAllFiveHighUrgencyAssignmentScenarios() throws Exception {
        long sectorId = createSector("Forjaria");
        long equipmentId = createEquipment(sectorId, "FOR-001");
        long technicianId = createTechnician("rn001@example.com");

        long lowRequestId = createMaintenanceRequest(equipmentId, sectorId, "LOW");
        assertEquals(200, assign(lowRequestId, technicianId).getResponse().getStatus());
        assertWorkload(0);

        long firstHighRequestId = createMaintenanceRequest(equipmentId, sectorId, "HIGH");
        long secondHighRequestId = createMaintenanceRequest(equipmentId, sectorId, "HIGH");
        long thirdHighRequestId = createMaintenanceRequest(equipmentId, sectorId, "HIGH");

        assertEquals(200, assign(firstHighRequestId, technicianId).getResponse().getStatus());
        assertWorkload(1);
        assertEquals(200, assign(secondHighRequestId, technicianId).getResponse().getStatus());
        assertWorkload(2);

        JsonNode rejection = body(assign(thirdHighRequestId, technicianId));
        assertEquals(409, rejection.get("status").asInt());
        assertEquals("HIGH_URGENCY_LIMIT", rejection.get("code").asText());
        assertEquals(
            "O técnico não pode ter mais de 2 chamados de urgência Alta abertos ao mesmo tempo.",
            rejection.get("message").asText()
        );

        assertEquals(200, updateStatus(firstHighRequestId, "CLOSED").getResponse().getStatus());
        assertWorkload(1);
        assertEquals(200, assign(thirdHighRequestId, technicianId).getResponse().getStatus());
        assertWorkload(2);
    }

    @Test
    void shouldRejectReassignmentWhenTargetTechnicianAlreadyHasTwoHighRequests() throws Exception {
        long sectorId = createSector("Soldagem");
        long equipmentId = createEquipment(sectorId, "SOL-001");
        long fullTechnicianId = createTechnician("full@example.com");
        long otherTechnicianId = createTechnician("other@example.com");

        long firstRequestId = createMaintenanceRequest(equipmentId, sectorId, "HIGH");
        long secondRequestId = createMaintenanceRequest(equipmentId, sectorId, "HIGH");
        long reassignedRequestId = createMaintenanceRequest(equipmentId, sectorId, "HIGH");
        assertEquals(200, assign(firstRequestId, fullTechnicianId).getResponse().getStatus());
        assertEquals(200, assign(secondRequestId, fullTechnicianId).getResponse().getStatus());
        assertEquals(200, assign(reassignedRequestId, otherTechnicianId).getResponse().getStatus());

        assertEquals(409, assign(reassignedRequestId, fullTechnicianId).getResponse().getStatus());

        JsonNode requests = body(mockMvc.perform(get("/api/maintenance-requests")
                .contextPath("/api"))
            .andExpect(status().isOk())
            .andReturn());
        JsonNode reassignedRequest = findById(requests, reassignedRequestId);
        assertEquals(otherTechnicianId, reassignedRequest.get("technicianId").asLong());
    }

    @Test
    void shouldKeepAssignmentIdempotentForTheSameTechnician() throws Exception {
        long sectorId = createSector("Corte");
        long equipmentId = createEquipment(sectorId, "COR-001");
        long technicianId = createTechnician("same@example.com");
        long firstRequestId = createMaintenanceRequest(equipmentId, sectorId, "HIGH");
        long secondRequestId = createMaintenanceRequest(equipmentId, sectorId, "HIGH");

        assertEquals(200, assign(firstRequestId, technicianId).getResponse().getStatus());
        assertEquals(200, assign(secondRequestId, technicianId).getResponse().getStatus());
        assertEquals(200, assign(secondRequestId, technicianId).getResponse().getStatus());
        assertWorkload(2);
    }

    @Test
    void shouldSerializeConcurrentAssignmentsAndRejectOneAboveTheLimit() throws Exception {
        long sectorId = createSector("Laminação");
        long equipmentId = createEquipment(sectorId, "LAM-001");
        long technicianId = createTechnician("concurrent@example.com");
        long existingRequestId = createMaintenanceRequest(equipmentId, sectorId, "HIGH");
        long concurrentRequestA = createMaintenanceRequest(equipmentId, sectorId, "HIGH");
        long concurrentRequestB = createMaintenanceRequest(equipmentId, sectorId, "HIGH");
        assertEquals(200, assign(existingRequestId, technicianId).getResponse().getStatus());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Integer> first = executor.submit(() -> concurrentAssignmentStatus(
                concurrentRequestA,
                technicianId,
                ready,
                start
            ));
            Future<Integer> second = executor.submit(() -> concurrentAssignmentStatus(
                concurrentRequestB,
                technicianId,
                ready,
                start
            ));

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            List<Integer> statuses = new ArrayList<>(List.of(
                first.get(10, TimeUnit.SECONDS),
                second.get(10, TimeUnit.SECONDS)
            ));
            Collections.sort(statuses);
            assertEquals(List.of(200, 409), statuses);
        } finally {
            executor.shutdownNow();
        }

        assertWorkload(2);
    }

    private int concurrentAssignmentStatus(
        long requestId,
        long technicianId,
        CountDownLatch ready,
        CountDownLatch start
    ) throws Exception {
        ready.countDown();
        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Concurrent assignment start timed out");
        }
        return assign(requestId, technicianId).getResponse().getStatus();
    }

    private void assertWorkload(int expected) throws Exception {
        mockMvc.perform(get("/api/technicians").contextPath("/api"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].highUrgencyOpenRequests").value(expected));
    }

    private JsonNode findById(JsonNode array, long id) {
        for (JsonNode item : array) {
            if (item.get("id").asLong() == id) {
                return item;
            }
        }
        throw new AssertionError("Maintenance request not found in response: " + id);
    }
}
