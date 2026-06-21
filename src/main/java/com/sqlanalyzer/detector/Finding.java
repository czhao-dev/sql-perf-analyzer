package com.sqlanalyzer.detector;

/** A single bottleneck detected in an execution plan. */
public record Finding(
        FindingCategory category,
        Severity severity,
        String message,
        String relatedNodeType,
        String relatedRelation) {
}
