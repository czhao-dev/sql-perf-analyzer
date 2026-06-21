package com.sqlanalyzer.cli;

import com.sqlanalyzer.testsupport.AbstractIntegrationTest;
import com.sqlanalyzer.testsupport.DemoDataSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyzeCommandIT extends AbstractIntegrationTest {

    private static Path reportPath;

    @DynamicPropertySource
    static void reportOutput(DynamicPropertyRegistry registry) {
        try {
            reportPath = Files.createTempFile("analyze-report-", ".md");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        registry.add("report.output", () -> reportPath.toString());
        registry.add("analysis.min-calls", () -> "1");
        registry.add("analysis.min-mean-time", () -> "0ms");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AnalyzeCommand analyzeCommand;

    @BeforeEach
    void seedAndWarmStatements() {
        jdbcTemplate.execute("SELECT pg_stat_statements_reset()");
        DemoDataSeeder.seed(jdbcTemplate);
        for (int i = 0; i < 5; i++) {
            jdbcTemplate.queryForList("SELECT * FROM orders WHERE customer_id = 1");
        }
    }

    @Test
    void writesMarkdownReportForCollectedQueries() throws IOException {
        int exitCode = analyzeCommand.run(new DefaultApplicationArguments());

        assertThat(exitCode).isEqualTo(0);
        String report = Files.readString(reportPath);
        assertThat(report).contains("# SQL Query Performance Report");
        assertThat(report).contains("orders");
    }
}
