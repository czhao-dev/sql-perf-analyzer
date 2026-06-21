package com.sqlanalyzer.index;

import com.sqlanalyzer.explain.PlanNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexRecommenderTest {

    @Mock
    private ExistingIndexInspector existingIndexInspector;

    @Test
    void recommendsIndexWhenNoExistingIndexCoversTheFilterColumn() {
        when(existingIndexInspector.forTable("orders")).thenReturn(List.of());
        IndexRecommender recommender = new IndexRecommender(existingIndexInspector);

        PlanNode seqScan = scanNode("orders", "(customer_id = 42)");

        List<IndexRecommendation> recommendations = recommender.recommend(seqScan);

        assertThat(recommendations).hasSize(1);
        assertThat(recommendations.get(0).table()).isEqualTo("orders");
        assertThat(recommendations.get(0).columns()).containsExactly("customer_id");
        assertThat(recommendations.get(0).ddl()).contains("CREATE INDEX").contains("orders").contains("customer_id");
    }

    @Test
    void skipsRecommendationWhenAnExistingIndexAlreadyCoversTheColumn() {
        ExistingIndex existing = new ExistingIndex("public", "orders", "idx_orders_customer_id",
                List.of("customer_id"), false, "CREATE INDEX idx_orders_customer_id ON orders (customer_id)");
        when(existingIndexInspector.forTable("orders")).thenReturn(List.of(existing));
        IndexRecommender recommender = new IndexRecommender(existingIndexInspector);

        PlanNode seqScan = scanNode("orders", "(customer_id = 42)");

        List<IndexRecommendation> recommendations = recommender.recommend(seqScan);

        assertThat(recommendations).isEmpty();
    }

    private PlanNode scanNode(String relation, String filter) {
        return new PlanNode("Seq Scan", relation, relation, null, 0, 100, 1000, 32,
                1000L, 1L, 0.0, 1.0, null, List.of(), filter, null, null, null, List.of());
    }
}
