package com.sqlanalyzer.explain;

/** Known PostgreSQL EXPLAIN node types the detectors look for, with a fallback for newer/unmapped types. */
public enum PlanNodeType {
    SEQ_SCAN("Seq Scan"),
    INDEX_SCAN("Index Scan"),
    INDEX_ONLY_SCAN("Index Only Scan"),
    BITMAP_HEAP_SCAN("Bitmap Heap Scan"),
    BITMAP_INDEX_SCAN("Bitmap Index Scan"),
    NESTED_LOOP("Nested Loop"),
    HASH_JOIN("Hash Join"),
    MERGE_JOIN("Merge Join"),
    SORT("Sort"),
    AGGREGATE("Aggregate"),
    LIMIT("Limit"),
    GATHER("Gather"),
    OTHER("");

    private final String label;

    PlanNodeType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static PlanNodeType fromLabel(String label) {
        for (PlanNodeType type : values()) {
            if (type.label.equals(label)) {
                return type;
            }
        }
        return OTHER;
    }
}
