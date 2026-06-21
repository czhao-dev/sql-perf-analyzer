package com.sqlanalyzer.detector;

import com.sqlanalyzer.config.AnalyzerProperties;
import com.sqlanalyzer.explain.ExplainPlan;
import com.sqlanalyzer.explain.PlanNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RowEstimationDetector implements PlanDetector {

    @Override
    public List<Finding> detect(ExplainPlan plan, AnalyzerProperties.Analysis thresholds) {
        List<Finding> findings = new ArrayList<>();
        if (!thresholds.detectRowEstimationError()) {
            return findings;
        }

        for (PlanNode node : plan.root().flatten()) {
            if (node.actualRows() == null || node.planRows() <= 0) {
                continue;
            }
            double ratio = (double) node.actualRows() / (double) node.planRows();
            double errorMagnitude = ratio >= 1 ? ratio : 1 / ratio;
            if (errorMagnitude < thresholds.rowEstimationErrorThreshold()) {
                continue;
            }
            findings.add(new Finding(
                    FindingCategory.ROW_ESTIMATION_MISMATCH,
                    Severity.WARNING,
                    String.format("Row estimation mismatch on %s: estimated %d rows, actual %d rows (%.1fx)",
                            node.nodeType(), node.planRows(), node.actualRows(), errorMagnitude),
                    node.nodeType(),
                    node.relationName()));
        }
        return findings;
    }
}
