package com.sqlanalyzer.explain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlanalyzer.testsupport.FixtureLoader;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlanJsonParserTest {

    private final PlanJsonParser parser = new PlanJsonParser(new ObjectMapper());

    @Test
    void parsesSeqScanFixtureIntoPlanTree() {
        ExplainPlan plan = parser.parse(FixtureLoader.load("seq_scan.json"));

        assertThat(plan.root().nodeType()).isEqualTo("Seq Scan");
        assertThat(plan.root().relationName()).isEqualTo("orders");
        assertThat(plan.root().actualRows()).isEqualTo(5820L);
        assertThat(plan.root().filter()).isEqualTo("(customer_id = 42)");
        assertThat(plan.planningTimeMs()).isEqualTo(0.085);
        assertThat(plan.executionTimeMs()).isEqualTo(12.612);
    }

    @Test
    void parsesNestedPlanChildren() {
        ExplainPlan plan = parser.parse(FixtureLoader.load("nested_loop.json"));

        assertThat(plan.root().nodeType()).isEqualTo("Nested Loop");
        assertThat(plan.root().children()).hasSize(2);
        assertThat(plan.root().children().get(1).indexName()).isEqualTo("idx_orders_customer_id");
    }

    @Test
    void parsesSortKeyArray() {
        ExplainPlan plan = parser.parse(FixtureLoader.load("sort.json"));

        assertThat(plan.root().nodeType()).isEqualTo("Sort");
        assertThat(plan.root().sortKey()).containsExactly("orders.created_at DESC");
    }
}
