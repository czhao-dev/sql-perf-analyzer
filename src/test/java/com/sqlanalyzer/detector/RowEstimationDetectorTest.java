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

class RowEstimationDetectorTest {

    private final PlanJsonParser parser = new PlanJsonParser(new ObjectMapper());
    private final RowEstimationDetector detector = new RowEstimationDetector();

    @Test
    void flagsRowEstimationMismatchAboveThreshold() {
        AnalyzerProperties.Analysis thresholds =
                new AnalyzerProperties.Analysis("pg_stat_statements", Duration.ofMillis(50), 5, 20, false, true, 10.0);
        ExplainPlan plan = parser.parse(FixtureLoader.load("row_estimation_mismatch.json"));

        List<Finding> findings = detector.detect(plan, thresholds);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).category()).isEqualTo(FindingCategory.ROW_ESTIMATION_MISMATCH);
        assertThat(findings.get(0).message()).contains("46.6x");
    }

    @Test
    void respectsDetectRowEstimationErrorToggle() {
        AnalyzerProperties.Analysis disabled =
                new AnalyzerProperties.Analysis("pg_stat_statements", Duration.ofMillis(50), 5, 20, false, false, 10.0);
        ExplainPlan plan = parser.parse(FixtureLoader.load("row_estimation_mismatch.json"));

        List<Finding> findings = detector.detect(plan, disabled);

        assertThat(findings).isEmpty();
    }
}
