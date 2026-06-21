package com.sqlanalyzer.explain;

import com.sqlanalyzer.testsupport.AbstractIntegrationTest;
import com.sqlanalyzer.testsupport.DemoDataSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class ExplainRunnerIT extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ExplainRunner explainRunner;

    @BeforeEach
    void seedDemoData() {
        DemoDataSeeder.seed(jdbcTemplate);
    }

    @Test
    void parsesRealSeqScanPlanFromPostgres() {
        ExplainPlan plan = explainRunner.explain("SELECT * FROM orders WHERE customer_id = 1", false);

        assertThat(plan.root().flatten())
                .anySatisfy(node -> assertThat(node.nodeType()).isEqualTo("Seq Scan"));
    }

    @Test
    void runsExplainAnalyzeAndCapturesActualRows() {
        ExplainPlan plan = explainRunner.explain("SELECT * FROM orders WHERE customer_id = 1", true);

        assertThat(plan.root().hasActualStats()).isTrue();
        assertThat(plan.executionTimeMs()).isNotNull();
    }
}
