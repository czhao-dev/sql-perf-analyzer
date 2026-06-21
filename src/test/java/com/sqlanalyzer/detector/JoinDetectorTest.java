package com.sqlanalyzer.detector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlanalyzer.config.AnalyzerProperties;
import com.sqlanalyzer.explain.ExplainPlan;
import com.sqlanalyzer.explain.PlanJsonParser;
import com.sqlanalyzer.testsupport.FixtureLoader;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JoinDetectorTest {

    private final PlanJsonParser parser = new PlanJsonParser(new ObjectMapper());
    private final JoinDetector detector = new JoinDetector();
    private final AnalyzerProperties.Analysis thresholds =
            new AnalyzerProperties.Analysis("pg_stat_statements", Duration.ofMillis(50), 5, 20, false, true, 10.0);

    @Test
    void flagsLargeNestedLoop() {
        ExplainPlan plan = parser.parse(FixtureLoader.load("nested_loop.json"));

        List<Finding> findings = detector.detect(plan, thresholds);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).category()).isEqualTo(FindingCategory.NESTED_LOOP);
    }

    @Test
    void reportsHashJoinInformationally() {
        ExplainPlan plan = parser.parse(FixtureLoader.load("high_cost.json"));

        List<Finding> findings = detector.detect(plan, thresholds);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).category()).isEqualTo(FindingCategory.HASH_JOIN);
        assertThat(findings.get(0).severity()).isEqualTo(Severity.INFO);
        assertThat(findings.get(0).message()).contains("orders.product_id = products.id");
    }
}
