package com.industriaplus.backend;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.*;

/** Executar explicitamente com -Dtest=MySqlUpgradeIT em um schema novo e isolado. */
class MySqlUpgradeIT {
    @Test
    void shouldUpgradeN1WithExistingTechnicianAndRequestWithoutLosingLinks() throws Exception {
        String url = System.getenv("MYSQL_UPGRADE_URL");
        assertNotNull(url, "Defina MYSQL_UPGRADE_URL para um banco de teste novo.");
        assertTrue(url.matches("jdbc:mysql://127\\.0\\.0\\.1:3309/industria_n3_upgrade_test_[0-9]+(?:\\?.*)?"),
            "Este teste só aceita o schema isolado de upgrade na porta 3309.");
        String user = System.getenv("MYSQL_UPGRADE_USER");
        String password = System.getenv("MYSQL_UPGRADE_PASSWORD");
        Flyway.configure().dataSource(url, user, password).target("2").load().migrate();
        try (var connection = DriverManager.getConnection(url, user, password);
                var sql = connection.createStatement()) {
            sql.executeUpdate("insert into sectors(id,name) values(900,'Setor existente')");
            sql.executeUpdate("insert into equipments(id,asset_tag,name,sector_id) values(900,'UPGRADE-900','Prensa existente',900)");
            sql.executeUpdate("insert into technicians(id,name,email,specialty) values(900,'Técnico existente','migrado@upgrade.test','Elétrica')");
            sql.executeUpdate("""
                insert into maintenance_requests(id,title,description,equipment_id,sector_id,technician_id,request_type,urgency,status)
                values(900,'Chamado existente','Dados de teste de migração.',900,900,900,'CORRECTIVE','HIGH','OPEN')
                """);
        }
        Flyway.configure().dataSource(url, user, password).load().migrate();
        try (var connection = DriverManager.getConnection(url, user, password);
                var sql = connection.createStatement()) {
            try (var rows = sql.executeQuery("""
                select r.status, u.name, u.role, u.password_hash, e.asset_tag
                from maintenance_requests r join app_users u on u.id=r.technician_id
                join equipments e on e.id=r.equipment_id where r.id=900
                """)) {
                assertTrue(rows.next());
                assertEquals("OPEN", rows.getString(1));
                assertEquals("Técnico existente", rows.getString(2));
                assertEquals("TECHNICIAN", rows.getString(3));
                assertEquals("MIGRATED-WITHOUT-PASSWORD", rows.getString(4));
                assertEquals("UPGRADE-900", rows.getString(5));
            }
            try (var rows = sql.executeQuery("select count(*) from preventive_plans")) {
                assertTrue(rows.next());
                assertEquals(0, rows.getInt(1));
            }
        }
    }
}
