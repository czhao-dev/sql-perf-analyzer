package com.sqlanalyzer.explain;

/** A fully parsed EXPLAIN result: the plan tree plus top-level planning/execution timings. */
public record ExplainPlan(PlanNode root, Double planningTimeMs, Double executionTimeMs, String rawJson) {
}
