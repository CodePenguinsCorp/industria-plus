package com.industriaplus.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

class MigrationCompatibilityTest {
    @Test
    void shouldAddAuthenticationWithoutLosingExistingEquipmentAndRequests() throws Exception {
        String url = "jdbc:h2:mem:migration-compatibility;MODE=MySQL;DB_CLOSE_DELAY=-1";
        Flyway.configure().dataSource(url, "sa", "").target("2").load().migrate();
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.createStatement()) {
            statement.executeUpdate("insert into sectors(id,name) values(1,'Produção')");
            statement.executeUpdate(
                    "insert into equipments(id,asset_tag,name,sector_id)"
                        + " values(1,'MAQ-OLD','Máquina existente',1)");
            statement.executeUpdate(
                    "insert into"
                        + " maintenance_requests(id,title,description,equipment_id,sector_id,request_type,urgency,status)"
                        + " values(1,'Chamado antigo','Descrição"
                        + " existente',1,1,'CORRECTIVE','HIGH','OPEN')");
        }
        Flyway.configure().dataSource(url, "sa", "").load().migrate();
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.createStatement()) {
            try (var rows = statement.executeQuery("select name from equipments where id=1")) {
                rows.next();
                assertEquals("Máquina existente", rows.getString(1));
            }
            try (var rows =
                    statement.executeQuery("select status from maintenance_requests where id=1")) {
                rows.next();
                assertEquals("OPEN", rows.getString(1));
            }
            try (var rows = statement.executeQuery("select count(*) from app_users")) {
                rows.next();
                assertEquals(0, rows.getInt(1));
            }
        }
    }

    @Test
    void shouldMoveTechniciansIntoUsersKeepingRequestsAndHistory() throws Exception {
        String url = "jdbc:h2:mem:migration-technicians;MODE=MySQL;DB_CLOSE_DELAY=-1";
        Flyway.configure().dataSource(url, "sa", "").target("3").load().migrate();
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.createStatement()) {
            statement.executeUpdate("insert into sectors(id,name) values(1,'Produção')");
            statement.executeUpdate(
                    "insert into equipments(id,asset_tag,name,sector_id) values(1,'MAQ-1','Prensa',1)");
            statement.executeUpdate(
                    "insert into technicians(id,name,email,specialty) values"
                            + " (10,'Ana','ana@x.com','Elétrica'),"
                            + " (11,'Bruno','bruno@x.com','Mecânica'),"
                            + " (12,'Carla','gestor@x.com',null)");
            statement.executeUpdate(
                    "insert into app_users(id,name,email,password_hash,role,technician_id) values"
                            + " (1,'Gestor','gestor@x.com','hash','MANAGER',null),"
                            + " (2,'Ana Login','ana.login@x.com','hash','TECHNICIAN',10)");
            statement.executeUpdate(
                    "insert into maintenance_requests"
                            + "(id,title,description,equipment_id,sector_id,technician_id,request_type,urgency,status)"
                            + " values (100,'R1','Descrição',1,1,10,'CORRECTIVE','HIGH','OPEN'),"
                            + " (101,'R2','Descrição',1,1,11,'CORRECTIVE','LOW','OPEN')");
            statement.executeUpdate(
                    "insert into maintenance_history(request_id,event_type,actor,description,"
                            + "previous_technician_id,previous_technician_name,current_technician_id,"
                            + "current_technician_name) values (100,'ASSIGNMENT','admin','x',11,'Bruno',10,'Ana')");
        }

        Flyway.configure().dataSource(url, "sa", "").load().migrate();

        try (var connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.createStatement()) {
            try (var rows =
                    statement.executeQuery(
                            "select count(*) from information_schema.tables"
                                    + " where lower(table_name) = 'technicians'")) {
                rows.next();
                assertEquals(0, rows.getInt(1));
            }
            try (var rows =
                    statement.executeQuery(
                            "select specialty, must_change_password from app_users where id = 2")) {
                rows.next();
                assertEquals("Elétrica", rows.getString(1));
                assertEquals(false, rows.getBoolean(2));
            }
            long bruno;
            try (var rows =
                    statement.executeQuery(
                            "select id, role, specialty, must_change_password from app_users"
                                    + " where email = 'bruno@x.com'")) {
                rows.next();
                bruno = rows.getLong(1);
                assertEquals("TECHNICIAN", rows.getString(2));
                assertEquals("Mecânica", rows.getString(3));
                assertEquals(true, rows.getBoolean(4));
            }
            try (var rows =
                    statement.executeQuery(
                            "select count(*) from app_users"
                                    + " where email = 'tecnico-12@migracao.invalid' and name = 'Carla'")) {
                rows.next();
                assertEquals(1, rows.getInt(1));
            }
            try (var rows =
                    statement.executeQuery(
                            "select id, technician_id from maintenance_requests order by id")) {
                rows.next();
                assertEquals(2L, rows.getLong(2));
                rows.next();
                assertEquals(bruno, rows.getLong(2));
            }
            try (var rows =
                    statement.executeQuery(
                            "select previous_technician_id, current_technician_id,"
                                    + " previous_technician_name from maintenance_history")) {
                rows.next();
                assertEquals(bruno, rows.getLong(1));
                assertEquals(2L, rows.getLong(2));
                assertEquals("Bruno", rows.getString(3));
            }
        }
    }
}
