package com.sqlanalyzer.index;

import java.util.List;

/** A raw, unvalidated index candidate pulled from a plan, before checking existing indexes. */
record IndexCandidate(String table, List<String> columns, IndexRecommendationKind kind, String reasoning) {
}
