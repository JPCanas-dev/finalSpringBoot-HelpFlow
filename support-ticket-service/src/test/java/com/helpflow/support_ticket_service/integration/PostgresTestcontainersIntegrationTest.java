package com.helpflow.support_ticket_service.integration;

import com.helpflow.support_ticket_service.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

// Prueba de integracion con Testcontainers de PostgreSQL
// Se activa en entornos con Docker o CI/CD (DOCKER_AVAILABLE=true)
@Testcontainers
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@EnabledIfEnvironmentVariable(named = "DOCKER_AVAILABLE", matches = "true")
class PostgresTestcontainersIntegrationTest {

    @Test
    void testcontainersDatabaseStartsSuccessfully() {
        assertTrue(true, "Contenedor de PostgreSQL iniciado correctamente mediante Testcontainers");
    }
}
