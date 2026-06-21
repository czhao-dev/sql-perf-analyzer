package com.sqlanalyzer.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlanalyzer.collector.QueryStat;
import com.sqlanalyzer.detector.Finding;
import com.sqlanalyzer.detector.FindingCategory;
import com.sqlanalyzer.detector.Severity;
import com.sqlanalyzer.explain.ExplainPlan;
import com.sqlanalyzer.explain.PlanJsonParser;
import com.sqlanalyzer.index.Confidence;
import com.sqlanalyzer.index.IndexRecommendation;
import com.sqlanalyzer.index.IndexRecommendationKind;
import com.sqlanalyzer.testsupport.FixtureLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownReportGeneratorTest {

    private final PlanJsonParser parser = new PlanJsonParser(new ObjectMapper());
    private final MarkdownReportGenerator generator = new MarkdownReportGenerator();

    @Test
    void rendersFullAnalysisReportWithFindingsAndRecommendations() {
        ExplainPlan plan = parser.parse(FixtureLoader.load("row_estimation_mismatch.json"));
        QueryStat stat = new QueryStat(872193, "SELECT * FROM orders WHERE customer_id = $1 ORDER BY created_at DESC",
                1520, 280064.0, 184.2, 500.0, 5820);
        Finding finding = new Finding(FindingCategory.ROW_ESTIMATION_MISMATCH, Severity.WARNING,
                "Row estimation mismatch on Seq Scan: estimated 125 rows, actual 5820 rows (46.6x)", "Seq Scan", "orders");
        IndexRecommendation recommendation = new IndexRecommendation("orders", List.of("customer_id", "created_at"),
                "CREATE INDEX idx_orders_customer_id_created_at ON orders (customer_id, created_at);",
                "Filter column \"customer_id\" used in a sequential scan on orders",
                "Indexes improve reads but add write overhead.", Confidence.HIGH, IndexRecommendationKind.FILTER);
        AnalysisResult result = new AnalysisResult(stat, plan, List.of(finding), List.of(recommendation),
                List.of("Validate with EXPLAIN ANALYZE before applying in production."));

        String report = generator.generateAnalysisReport(List.of(result));

        assertThat(report)
                .contains("Query ID: 872193")
                .contains("Mean Time: 184.2 ms")
                .contains("Calls: 1520")
                .contains("Seq Scan on orders")
                .contains("Row estimation mismatch on Seq Scan")
                .contains("CREATE INDEX idx_orders_customer_id_created_at")
                .contains("Validate with EXPLAIN ANALYZE before applying in production.");
    }

    @Test
    void rendersUnavailablePlanSummaryWhenExplainPlanIsNull() {
        QueryStat stat = new QueryStat(1, "SELECT * FROM orders WHERE customer_id = $1", 10, 100.0, 10.0, 20.0, 50);
        AnalysisResult result = new AnalysisResult(stat, null, List.of(), List.of(), List.of());

        String report = generator.generateAnalysisReport(List.of(result));

        assertThat(report).contains("Plan Summary:\n- (unavailable)");
    }
}
