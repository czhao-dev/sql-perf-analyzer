package com.sqlanalyzer.detector;

import com.sqlanalyzer.config.AnalyzerProperties;
import com.sqlanalyzer.explain.ExplainPlan;
import com.sqlanalyzer.explain.PlanNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JoinDetector implements PlanDetector {

    private static final long LARGE_NESTED_LOOP_ROW_THRESHOLD = 1000;

    @Override
    public List<Finding> detect(ExplainPlan plan, AnalyzerProperties.Analysis thresholds) {
        List<Finding> findings = new ArrayList<>();
        for (PlanNode node : plan.root().flatten()) {
            switch (node.nodeType()) {
                case "Nested Loop" -> {
                    long rows = node.actualRows() != null ? node.actualRows() : node.planRows();
                    if (rows >= LARGE_NESTED_LOOP_ROW_THRESHOLD) {
                        findings.add(new Finding(
                                FindingCategory.NESTED_LOOP,
                                Severity.WARNING,
                                "Nested loop join may be inefficient over " + rows + " rows",
                                node.nodeType(),
                                node.relationName()));
                    }
                }
                case "Hash Join" -> findings.add(new Finding(
                        FindingCategory.HASH_JOIN,
                        Severity.INFO,
                        "Hash join detected" + (node.hashCond() != null ? " on " + node.hashCond() : ""),
                        node.nodeType(),
                        node.relationName()));
                case "Merge Join" -> findings.add(new Finding(
                        FindingCategory.MERGE_JOIN,
                        Severity.INFO,
                        "Merge join detected" + (node.mergeCond() != null ? " on " + node.mergeCond() : ""),
                        node.nodeType(),
                        node.relationName()));
                default -> {
                }
            }
        }
        return findings;
    }
}
