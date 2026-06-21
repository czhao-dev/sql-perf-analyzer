package com.sqlanalyzer.report;

import com.sqlanalyzer.explain.ExplainPlan;
import com.sqlanalyzer.explain.PlanNode;

import java.util.ArrayList;
import java.util.List;

public record ComparisonResult(
        ExplainPlan before,
        ExplainPlan after,
        double costDeltaPercent,
        Double timeDeltaPercent,
        List<String> nodeTypeChanges) {

    public static ComparisonResult of(ExplainPlan before, ExplainPlan after) {
        double beforeCost = before.root().totalCost();
        double afterCost = after.root().totalCost();
        double costDelta = beforeCost == 0 ? 0 : ((afterCost - beforeCost) / beforeCost) * 100;

        Double timeDelta = null;
        if (before.executionTimeMs() != null && after.executionTimeMs() != null && before.executionTimeMs() != 0) {
            timeDelta = ((after.executionTimeMs() - before.executionTimeMs()) / before.executionTimeMs()) * 100;
        }

        return new ComparisonResult(before, after, costDelta, timeDelta, nodeTypeChanges(before.root(), after.root()));
    }

    private static List<String> nodeTypeChanges(PlanNode beforeRoot, PlanNode afterRoot) {
        List<PlanNode> beforeNodes = beforeRoot.flatten();
        List<PlanNode> afterNodes = afterRoot.flatten();
        List<String> changes = new ArrayList<>();

        int max = Math.min(beforeNodes.size(), afterNodes.size());
        for (int i = 0; i < max; i++) {
            String beforeType = beforeNodes.get(i).nodeType();
            String afterType = afterNodes.get(i).nodeType();
            if (!beforeType.equals(afterType)) {
                String relation = afterNodes.get(i).relationName() != null
                        ? afterNodes.get(i).relationName()
                        : beforeNodes.get(i).relationName();
                changes.add(beforeType + " -> " + afterType + (relation != null ? " on " + relation : ""));
            }
        }
        return List.copyOf(changes);
    }
}
