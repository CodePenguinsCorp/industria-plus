package com.industriaplus.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CatalogApiIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    void shouldCreateAndListNormalizedCatalogResources() throws Exception {
        long sectorId = body(mockMvc.perform(post("/api/sectors")
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "name", "  Usinagem  ",
                    "description", "   "
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Usinagem"))
            .andExpect(jsonPath("$.description").doesNotExist())
            .andExpect(jsonPath("$.createdAt").isNotEmpty())
            .andReturn()).get("id").asLong();

        mockMvc.perform(post("/api/equipments")
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "assetTag", "  EQ-001  ",
                    "name", "  Torno CNC  ",
                    "description", "  Torno principal  ",
                    "sectorId", sectorId
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.assetTag").value("EQ-001"))
            .andExpect(jsonPath("$.name").value("Torno CNC"))
            .andExpect(jsonPath("$.description").value("Torno principal"))
            .andExpect(jsonPath("$.sectorId").value(sectorId))
            .andExpect(jsonPath("$.sectorName").value("Usinagem"))
            .andExpect(jsonPath("$.createdAt").isNotEmpty());

        long managerId = createManager("gestor@example.com");
        mockMvc.perform(post("/api/users")
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "name", "  Ana Técnica  ",
                    "email", "  ANA@EXAMPLE.COM  ",
                    "password", TEMPORARY_PASSWORD,
                    "role", "TECHNICIAN",
                    "managerId", managerId,
                    "specialty", "  Elétrica  "
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Ana Técnica"))
            .andExpect(jsonPath("$.email").value("ana@example.com"))
            .andExpect(jsonPath("$.specialty").value("Elétrica"))
            .andExpect(jsonPath("$.managerId").value(managerId))
            .andExpect(jsonPath("$.mustChangePassword").value(true))
            .andExpect(jsonPath("$.createdAt").isNotEmpty());

        mockMvc.perform(get("/api/sectors").contextPath("/api"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Usinagem"));
        mockMvc.perform(get("/api/equipments").contextPath("/api"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].assetTag").value("EQ-001"));
        mockMvc.perform(get("/api/technicians").contextPath("/api"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].email").value("ana@example.com"))
            .andExpect(jsonPath("$[0].specialty").value("Elétrica"))
            .andExpect(jsonPath("$[0].managerName").value("Gestor de manutenção"))
            .andExpect(jsonPath("$[0].highUrgencyOpenRequests").value(0))
            .andExpect(jsonPath("$[0].createdAt").isNotEmpty());
    }

    @Test
    void shouldRejectDuplicateAssetTagAndUserEmail() throws Exception {
        long sectorId = createSector("Montagem");
        createEquipment(sectorId, "EQ-100");
        createTechnician("joao@example.com");

        mockMvc.perform(post("/api/equipments")
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "assetTag", "eq-100",
                    "name", "Outro equipamento",
                    "sectorId", sectorId
                ))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("DUPLICATE_ASSET_TAG"));

        mockMvc.perform(post("/api/users")
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "name", "Outro gestor",
                    "email", "JOAO@EXAMPLE.COM",
                    "password", TEMPORARY_PASSWORD,
                    "role", "MANAGER"
                ))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("DUPLICATE_USER_EMAIL"));
    }

    @Test
    void shouldValidateMinimumLengthsAfterWhitespaceIsRemoved() throws Exception {
        mockMvc.perform(post("/api/sectors")
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("name", " a "))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fieldErrors.name").exists());

        mockMvc.perform(post("/api/equipments")
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "assetTag", " x ",
                    "name", " y ",
                    "sectorId", 1
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.assetTag").exists())
            .andExpect(jsonPath("$.fieldErrors.name").exists());

        mockMvc.perform(post("/api/users")
                .contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "name", "Técnico",
                    "email", "not-an-email",
                    "password", "curta",
                    "role", "TECHNICIAN"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fieldErrors.email").exists())
            .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void shouldDeleteCatalogResourcesWithoutLinks() throws Exception {
        long sectorId = createSector("Almoxarifado");
        long equipmentId = createEquipment(sectorId, "EQ-DELETE");
        long technicianId = createTechnician("delete@example.com");

        mockMvc.perform(delete("/api/technicians/{id}", technicianId).contextPath("/api"))
            .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/equipments/{id}", equipmentId).contextPath("/api"))
            .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/sectors/{id}", sectorId).contextPath("/api"))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/sectors").contextPath("/api"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/equipments").contextPath("/api"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/technicians").contextPath("/api"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldRejectDeletingCatalogResourcesWithLinks() throws Exception {
        long sectorId = createSector("Produção");
        long equipmentId = createEquipment(sectorId, "EQ-IN-USE");
        long technicianId = createTechnician("in-use@example.com");
        long requestId = createMaintenanceRequest(equipmentId, sectorId, "MEDIUM");
        assign(requestId, technicianId).getResponse();

        mockMvc.perform(delete("/api/sectors/{id}", sectorId).contextPath("/api"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("SECTOR_IN_USE"));
        mockMvc.perform(delete("/api/equipments/{id}", equipmentId).contextPath("/api"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("EQUIPMENT_IN_USE"));
        mockMvc.perform(delete("/api/technicians/{id}", technicianId).contextPath("/api"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("TECHNICIAN_IN_USE"));
    }
}
