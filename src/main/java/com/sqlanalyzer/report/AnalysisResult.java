package com.sqlanalyzer.report;

import com.sqlanalyzer.collector.QueryStat;
import com.sqlanalyzer.detector.Finding;
import com.sqlanalyzer.explain.ExplainPlan;
import com.sqlanalyzer.index.IndexRecommendation;

import java.util.List;

/**
 * The aggregate result of analyzing one query: its stats, parsed plan (null if EXPLAIN
 * could not be run, e.g. on a pg_stat_statements query with unresolved $N placeholders),
 * detected findings, index recommendations, and general risk notes.
 */
public record AnalysisResult(
        QueryStat queryStat,
        ExplainPlan explainPlan,
        List<Finding> findings,
        List<IndexRecommendation> recommendations,
        List<String> riskNotes) {
}
