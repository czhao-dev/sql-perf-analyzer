package com.sqlanalyzer.detector;

import com.sqlanalyzer.config.AnalyzerProperties;
import com.sqlanalyzer.explain.ExplainPlan;
import com.sqlanalyzer.explain.PlanNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HighCostNodeDetector implements PlanDetector {

    private static final double HIGH_COST_THRESHOLD = 10_000.0;

    @Override
    public List<Finding> detect(ExplainPlan plan, AnalyzerProperties.Analysis thresholds) {
        List<Finding> findings = new ArrayList<>();
        for (PlanNode node : plan.root().flatten()) {
            if (node.totalCost() < HIGH_COST_THRESHOLD) {
                continue;
            }
            findings.add(new Finding(
                    FindingCategory.HIGH_COST_NODE,
                    Severity.CRITICAL,
                    String.format("High-cost plan node: %s (total cost %.1f)", node.nodeType(), node.totalCost()),
                    node.nodeType(),
                    node.relationName()));
        }
        return findings;
    }
}
