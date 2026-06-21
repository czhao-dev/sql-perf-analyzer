package com.sqlanalyzer.testsupport;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Shared Testcontainers Postgres instance across every IT subclass, using the
 * "singleton container" pattern (manual start, no @Container/@Testcontainers
 * lifecycle annotations): those annotations stop the container after each
 * owning test class finishes, which breaks reuse across multiple IT classes.
 * Testcontainers' Ryuk reaper cleans this up automatically at JVM exit.
 */
@SpringBootTest
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:16")
                .withDatabaseName("demo")
                .withUsername("test")
                .withPassword("test")
                .withCommand("postgres", "-c", "shared_preload_libraries=pg_stat_statements");
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("database.url", () -> "postgres://" + POSTGRES.getUsername() + ":" + POSTGRES.getPassword()
                + "@" + POSTGRES.getHost() + ":" + POSTGRES.getMappedPort(5432) + "/" + POSTGRES.getDatabaseName()
                + "?sslmode=disable");
    }
}
