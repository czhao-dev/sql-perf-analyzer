package com.sqlanalyzer.detector;

public enum FindingCategory {
    SEQ_SCAN,
    NESTED_LOOP,
    EXPENSIVE_SORT,
    ROW_ESTIMATION_MISMATCH,
    HIGH_COST_NODE,
    HASH_JOIN,
    MERGE_JOIN,
    EXPLAIN_FAILED
}
