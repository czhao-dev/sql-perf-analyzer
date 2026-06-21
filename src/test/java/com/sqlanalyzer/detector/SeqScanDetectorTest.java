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

class SeqScanDetectorTest {

    private final PlanJsonParser parser = new PlanJsonParser(new ObjectMapper());
    private final SeqScanDetector detector = new SeqScanDetector();
    private final AnalyzerProperties.Analysis thresholds =
            new AnalyzerProperties.Analysis("pg_stat_statements", Duration.ofMillis(50), 5, 20, false, true, 10.0);

    @Test
    void flagsLargeSequentialScan() {
        ExplainPlan plan = parser.parse(FixtureLoader.load("seq_scan.json"));

        List<Finding> findings = detector.detect(plan, thresholds);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).category()).isEqualTo(FindingCategory.SEQ_SCAN);
        assertThat(findings.get(0).message()).contains("orders");
    }

    @Test
    void doesNotFlagSmallScansOrIndexScans() {
        // nested_loop.json's Seq Scan on customers is only 500 rows (below threshold),
        // and its Index Scan on orders isn't a Seq Scan at all.
        ExplainPlan plan = parser.parse(FixtureLoader.load("nested_loop.json"));

        List<Finding> findings = detector.detect(plan, thresholds);

        assertThat(findings).isEmpty();
    }
}
