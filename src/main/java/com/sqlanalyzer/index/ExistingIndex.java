package com.sqlanalyzer.index;

import java.util.List;

/** An index already present on a table, as reported by {@code pg_indexes}. */
public record ExistingIndex(
        String schema,
        String table,
        String indexName,
        List<String> columns,
        boolean unique,
        String indexDef) {
}
