package com.sqlanalyzer.index;

import com.sqlanalyzer.explain.PlanNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Combines plan-derived index candidates with existing-index inspection to produce de-duplicated recommendations. */
@Component
public class IndexRecommender {

    private static final String RISK_NOTE =
            "Indexes improve reads but add write overhead; validate with EXPLAIN ANALYZE before applying in production.";

    private final ExistingIndexInspector existingIndexInspector;
    private final IndexCandidateExtractor candidateExtractor = new IndexCandidateExtractor();

    public IndexRecommender(ExistingIndexInspector existingIndexInspector) {
        this.existingIndexInspector = existingIndexInspector;
    }

    public List<IndexRecommendation> recommend(PlanNode root) {
        List<IndexCandidate> candidates = candidateExtractor.extract(root);
        List<IndexRecommendation> recommendations = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (IndexCandidate candidate : candidates) {
            String dedupeKey = candidate.table() + ":" + candidate.columns();
            if (!seen.add(dedupeKey) || isAlreadyCovered(candidate)) {
                continue;
            }
            recommendations.add(toRecommendation(candidate));
        }
        return recommendations;
    }

    private boolean isAlreadyCovered(IndexCandidate candidate) {
        List<ExistingIndex> existingIndexes = existingIndexInspector.forTable(candidate.table());
        return existingIndexes.stream().anyMatch(existing ->
                existing.columns().size() >= candidate.columns().size()
                        && existing.columns().subList(0, candidate.columns().size()).equals(candidate.columns()));
    }

    private IndexRecommendation toRecommendation(IndexCandidate candidate) {
        String indexName = "idx_" + candidate.table() + "_" + String.join("_", candidate.columns());
        String ddl = "CREATE INDEX " + indexName + " ON " + candidate.table()
                + " (" + String.join(", ", candidate.columns()) + ");";
        Confidence confidence = candidate.kind() == IndexRecommendationKind.ORDER_BY ? Confidence.MEDIUM : Confidence.HIGH;

        return new IndexRecommendation(
                candidate.table(), candidate.columns(), ddl, candidate.reasoning(), RISK_NOTE, confidence, candidate.kind());
    }
}
