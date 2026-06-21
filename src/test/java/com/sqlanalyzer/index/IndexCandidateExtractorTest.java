package com.sqlanalyzer.index;

import com.sqlanalyzer.explain.PlanNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IndexCandidateExtractorTest {

    private final IndexCandidateExtractor extractor = new IndexCandidateExtractor();

    @Test
    void extractsFilterColumnFromSeqScan() {
        PlanNode seqScan = scanNode("Seq Scan", "orders", "(customer_id = 42)", null, null, List.of());

        List<IndexCandidate> candidates = extractor.extract(seqScan);

        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).table()).isEqualTo("orders");
        assertThat(candidates.get(0).columns()).containsExactly("customer_id");
        assertThat(candidates.get(0).kind()).isEqualTo(IndexRecommendationKind.FILTER);
    }

    @Test
    void extractsBothSidesOfJoinCondition() {
        PlanNode hashJoin = joinNode("Hash Join", "(orders.product_id = products.id)");

        List<IndexCandidate> candidates = extractor.extract(hashJoin);

        assertThat(candidates).hasSize(2);
        assertThat(candidates).anySatisfy(c -> {
            assertThat(c.table()).isEqualTo("orders");
            assertThat(c.columns()).containsExactly("product_id");
        });
        assertThat(candidates).anySatisfy(c -> {
            assertThat(c.table()).isEqualTo("products");
            assertThat(c.columns()).containsExactly("id");
        });
    }

    @Test
    void extractsOrderByColumnsFromSortNodeUsingQualifiedSortKey() {
        PlanNode scan = scanNode("Seq Scan", "orders", null, null, null, List.of());
        PlanNode sort = sortNode(List.of("orders.created_at DESC"), List.of(scan));

        List<IndexCandidate> candidates = extractor.extract(sort);

        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).table()).isEqualTo("orders");
        assertThat(candidates.get(0).columns()).containsExactly("created_at");
        assertThat(candidates.get(0).kind()).isEqualTo(IndexRecommendationKind.ORDER_BY);
    }

    @Test
    void infersTableFromDescendantScanWhenSortKeyIsUnqualified() {
        PlanNode scan = scanNode("Seq Scan", "orders", null, null, null, List.of());
        PlanNode sort = sortNode(List.of("created_at"), List.of(scan));

        List<IndexCandidate> candidates = extractor.extract(sort);

        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).table()).isEqualTo("orders");
        assertThat(candidates.get(0).columns()).containsExactly("created_at");
    }

    private PlanNode scanNode(String nodeType, String relation, String filter, String hashCond, String mergeCond,
                               List<String> sortKey) {
        return new PlanNode(nodeType, relation, relation, null, 0, 100, 1000, 32,
                1000L, 1L, 0.0, 1.0, null, sortKey, filter, hashCond, mergeCond, null, List.of());
    }

    private PlanNode joinNode(String nodeType, String hashCond) {
        return new PlanNode(nodeType, null, null, null, 0, 100, 1000, 32,
                1000L, 1L, 0.0, 1.0, "Inner", List.of(), null, hashCond, null, null, List.of());
    }

    private PlanNode sortNode(List<String> sortKey, List<PlanNode> children) {
        return new PlanNode("Sort", null, null, null, 0, 100, 1000, 32,
                1000L, 1L, 0.0, 1.0, null, sortKey, null, null, null, null, children);
    }
}
