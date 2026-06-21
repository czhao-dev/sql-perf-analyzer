package com.sqlanalyzer.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlanalyzer.collector.QueryStat;
import com.sqlanalyzer.explain.ExplainPlan;
import com.sqlanalyzer.explain.PlanJsonParser;
import com.sqlanalyzer.testsupport.FixtureLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonReportGeneratorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PlanJsonParser parser = new PlanJsonParser(objectMapper);
    private final JsonReportGenerator generator = new JsonReportGenerator(objectMapper);

    @Test
    void rendersAnalysisResultsAsJsonArray() throws Exception {
        ExplainPlan plan = parser.parse(FixtureLoader.load("seq_scan.json"));
        QueryStat stat = new QueryStat(1, "SELECT * FROM orders", 10, 100.0, 10.0, 20.0, 50);
        AnalysisResult result = new AnalysisResult(stat, plan, List.of(), List.of(), List.of());

        String json = generator.generateAnalysisReport(List.of(result));
        JsonNode tree = objectMapper.readTree(json);

        assertThat(tree.isArray()).isTrue();
        assertThat(tree.get(0).get("queryStat").get("queryId").asLong()).isEqualTo(1L);
        assertThat(tree.get(0).get("explainPlan").get("root").get("nodeType").asText()).isEqualTo("Seq Scan");
    }

    @Test
    void rendersComparisonResultAsJsonObject() throws Exception {
        ExplainPlan before = parser.parse(FixtureLoader.load("seq_scan.json"));
        ExplainPlan after = parser.parse(FixtureLoader.load("nested_loop.json"));
        ComparisonResult comparison = ComparisonResult.of(before, after);

        String json = generator.generateComparisonReport(comparison);
        JsonNode tree = objectMapper.readTree(json);

        assertThat(tree.has("costDeltaPercent")).isTrue();
        assertThat(tree.has("nodeTypeChanges")).isTrue();
    }
}
