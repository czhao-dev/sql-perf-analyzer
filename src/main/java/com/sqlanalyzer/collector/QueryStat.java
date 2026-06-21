package com.sqlanalyzer.collector;

/** A single row collected from {@code pg_stat_statements}. */
public record QueryStat(
        long queryId,
        String query,
        long calls,
        double totalExecTimeMs,
        double meanExecTimeMs,
        double maxExecTimeMs,
        long rows) {
}
