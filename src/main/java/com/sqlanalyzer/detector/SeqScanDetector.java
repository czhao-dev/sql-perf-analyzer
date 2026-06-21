package com.sqlanalyzer.detector;

import com.sqlanalyzer.config.AnalyzerProperties;
import com.sqlanalyzer.explain.ExplainPlan;
import com.sqlanalyzer.explain.PlanNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SeqScanDetector implements PlanDetector {

    private static final long LARGE_SCAN_ROW_THRESHOLD = 1000;

    @Override
    public List<Finding> detect(ExplainPlan plan, AnalyzerProperties.Analysis thresholds) {
        List<Finding> findings = new ArrayList<>();
        for (PlanNode node : plan.root().flatten()) {
            if (!"Seq Scan".equals(node.nodeType())) {
                continue;
            }
            long rows = node.actualRows() != null ? node.actualRows() : node.planRows();
            if (rows < LARGE_SCAN_ROW_THRESHOLD) {
                continue;
            }
            String relation = node.relationName() != null ? node.relationName() : "unknown relation";
            findings.add(new Finding(
                    FindingCategory.SEQ_SCAN,
                    Severity.WARNING,
                    "Large sequential scan detected on " + relation + " (" + rows + " rows)",
                    node.nodeType(),
                    node.relationName()));
        }
        return findings;
    }
}
