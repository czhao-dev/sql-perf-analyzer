package com.sqlanalyzer.explain;

import java.util.ArrayList;
import java.util.List;

/** A single node in a parsed {@code EXPLAIN (FORMAT JSON)} plan tree. */
public record PlanNode(
        String nodeType,
        String relationName,
        String alias,
        String indexName,
        double startupCost,
        double totalCost,
        long planRows,
        int planWidth,
        Long actualRows,
        Long actualLoops,
        Double actualStartupTimeMs,
        Double actualTotalTimeMs,
        String joinType,
        List<String> sortKey,
        String filter,
        String hashCond,
        String mergeCond,
        String indexCond,
        List<PlanNode> children) {

    public boolean hasActualStats() {
        return actualRows != null;
    }

    /** Pre-order traversal of this node and all descendants. */
    public List<PlanNode> flatten() {
        List<PlanNode> all = new ArrayList<>();
        collect(this, all);
        return all;
    }

    private static void collect(PlanNode node, List<PlanNode> acc) {
        acc.add(node);
        for (PlanNode child : node.children()) {
            collect(child, acc);
        }
    }
}
