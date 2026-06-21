package com.sqlanalyzer.report;

import com.sqlanalyzer.common.SqlRedactor;
import com.sqlanalyzer.detector.Finding;
import com.sqlanalyzer.explain.PlanNode;
import com.sqlanalyzer.index.IndexRecommendation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class MarkdownReportGenerator implements ReportGenerator {

    @Override
    public String generateAnalysisReport(List<AnalysisResult> results) {
        StringBuilder sb = new StringBuilder("# SQL Query Performance Report\n\n");
        for (AnalysisResult result : results) {
            appendResult(sb, result);
        }
        return sb.toString();
    }

    private void appendResult(StringBuilder sb, AnalysisResult result) {
        var stat = result.queryStat();
        sb.append("## Query ID: ").append(stat.queryId()).append('\n');
        sb.append("Mean Time: ").append(String.format(Locale.ROOT, "%.1f ms", stat.meanExecTimeMs())).append('\n');
        sb.append("Calls: ").append(stat.calls()).append('\n');
        sb.append("Total Time: ").append(String.format(Locale.ROOT, "%.1f ms", stat.totalExecTimeMs())).append("\n\n");

        sb.append("Query:\n").append(SqlRedactor.redact(stat.query())).append("\n\n");

        sb.append("Plan Summary:\n");
        if (result.explainPlan() != null) {
            appendPlanSummary(sb, result.explainPlan().root());
        } else {
            sb.append("- (unavailable)\n");
        }
        sb.append('\n');

        sb.append("Findings:\n");
        appendFindings(sb, result.findings());
        sb.append('\n');

        sb.append("Recommendations:\n");
        appendRecommendations(sb, result.recommendations());
        sb.append('\n');

        sb.append("Risk Notes:\n");
        for (String note : result.riskNotes()) {
            sb.append("- ").append(note).append('\n');
        }
        sb.append("\n---\n\n");
    }

    private void appendPlanSummary(StringBuilder sb, PlanNode root) {
        for (PlanNode node : root.flatten()) {
            sb.append("- ").append(node.nodeType());
            if (node.relationName() != null) {
                sb.append(" on ").append(node.relationName());
            }
            if (node.actualRows() != null) {
                sb.append(" (estimated ").append(node.planRows()).append(" rows, actual ")
                        .append(node.actualRows()).append(" rows)");
            }
            sb.append('\n');
        }
    }

    private void appendFindings(StringBuilder sb, List<Finding> findings) {
        if (findings.isEmpty()) {
            sb.append("- (none)\n");
            return;
        }
        for (Finding finding : findings) {
            sb.append("- ").append(finding.message()).append('\n');
        }
    }

    private void appendRecommendations(StringBuilder sb, List<IndexRecommendation> recommendations) {
        if (recommendations.isEmpty()) {
            sb.append("- (none)\n");
            return;
        }
        int i = 1;
        for (IndexRecommendation recommendation : recommendations) {
            sb.append(i++).append(". ").append(recommendation.reasoning()).append(":\n");
            sb.append("   ").append(recommendation.ddl()).append('\n');
        }
    }

    @Override
    public String generateComparisonReport(ComparisonResult comparison) {
        StringBuilder sb = new StringBuilder("# Plan Comparison\n\n");
        sb.append("Cost change: ").append(String.format(Locale.ROOT, "%.1f%%", comparison.costDeltaPercent())).append('\n');
        if (comparison.timeDeltaPercent() != null) {
            sb.append("Execution time change: ")
                    .append(String.format(Locale.ROOT, "%.1f%%", comparison.timeDeltaPercent())).append('\n');
        }
        sb.append('\n').append("Plan Node Changes:\n");
        if (comparison.nodeTypeChanges().isEmpty()) {
            sb.append("- (no node type changes)\n");
        } else {
            for (String change : comparison.nodeTypeChanges()) {
                sb.append("- ").append(change).append('\n');
            }
        }
        return sb.toString();
    }
}
