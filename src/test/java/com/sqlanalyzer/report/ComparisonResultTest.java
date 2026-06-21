package com.sqlanalyzer.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlanalyzer.explain.ExplainPlan;
import com.sqlanalyzer.explain.PlanJsonParser;
import com.sqlanalyzer.testsupport.FixtureLoader;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ComparisonResultTest {

    private final PlanJsonParser parser = new PlanJsonParser(new ObjectMapper());

    @Test
    void computesCostDeltaAndNodeTypeChangeBetweenSeqScanAndIndexScan() {
        ExplainPlan before = parser.parse(FixtureLoader.load("seq_scan.json"));
        ExplainPlan after = parser.parse(FixtureLoader.load("nested_loop.json"));

        ComparisonResult comparison = ComparisonResult.of(before, after);

        assertThat(comparison.costDeltaPercent()).isCloseTo(617.66, within(0.5));
        assertThat(comparison.nodeTypeChanges()).anySatisfy(change -> assertThat(change).contains("Seq Scan -> Nested Loop"));
    }
}
