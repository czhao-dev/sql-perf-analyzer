package com.sqlanalyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Mirrors the config.yaml shape from the project README: top-level
 * database/analysis/recommendations/report sections, each independently
 * overridable on the command line via Spring's standard
 * {@code --section.field=value} property binding (e.g. {@code --database.url=...}).
 */
@ConfigurationProperties(prefix = "")
public record AnalyzerProperties(
        Database database,
        Analysis analysis,
        Recommendations recommendations,
        Report report) {

    public record Database(
            String url,
            @DefaultValue("5s") Duration statementTimeout) {
    }

    public record Analysis(
            @DefaultValue("pg_stat_statements") String querySource,
            @DefaultValue("50ms") Duration minMeanTime,
            @DefaultValue("5") int minCalls,
            @DefaultValue("20") int limit,
            @DefaultValue("false") boolean runExplainAnalyze,
            @DefaultValue("true") boolean detectRowEstimationError,
            @DefaultValue("10.0") double rowEstimationErrorThreshold) {
    }

    public record Recommendations(
            @DefaultValue("true") boolean suggestIndexes,
            @DefaultValue("true") boolean checkExistingIndexes,
            @DefaultValue("true") boolean includeRiskNotes) {
    }

    public record Report(
            @DefaultValue("markdown") String format,
            @DefaultValue("report.md") String output) {
    }
}
