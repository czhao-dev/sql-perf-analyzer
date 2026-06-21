package com.sqlanalyzer.detector;

import com.sqlanalyzer.config.AnalyzerProperties;
import com.sqlanalyzer.explain.ExplainPlan;
import com.sqlanalyzer.explain.PlanNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SortDetector implements PlanDetector {

    private static final long EXPENSIVE_SORT_ROW_THRESHOLD = 1000;

    @Override
    public List<Finding> detect(ExplainPlan plan, AnalyzerProperties.Analysis thresholds) {
        List<Finding> findings = new ArrayList<>();
        for (PlanNode node : plan.root().flatten()) {
            if (!"Sort".equals(node.nodeType())) {
                continue;
            }
            long rows = node.actualRows() != null ? node.actualRows() : node.planRows();
            if (rows < EXPENSIVE_SORT_ROW_THRESHOLD) {
                continue;
            }
            String onColumns = node.sortKey().isEmpty() ? "" : " on " + String.join(", ", node.sortKey());
            findings.add(new Finding(
                    FindingCategory.EXPENSIVE_SORT,
                    Severity.WARNING,
                    "Expensive sort detected" + onColumns + " (" + rows + " rows)",
                    node.nodeType(),
                    node.relationName()));
        }
        return findings;
    }
}
