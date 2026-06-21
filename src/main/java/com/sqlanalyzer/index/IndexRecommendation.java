package com.sqlanalyzer.index;

import java.util.List;

public record IndexRecommendation(
        String table,
        List<String> columns,
        String ddl,
        String reasoning,
        String riskNote,
        Confidence confidence,
        IndexRecommendationKind kind) {
}
