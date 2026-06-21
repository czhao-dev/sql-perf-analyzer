package com.sqlanalyzer.index;

import com.sqlanalyzer.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExistingIndexInspectorIT extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ExistingIndexInspector inspector;

    @Test
    void findsPrimaryKeyIndexOnOrdersTable() {
        List<ExistingIndex> indexes = inspector.forTable("orders");

        assertThat(indexes).anySatisfy(index -> assertThat(index.columns()).containsExactly("id"));
    }

    @Test
    void findsManuallyCreatedCompositeIndex() {
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_orders_customer_created "
                + "ON orders (customer_id, created_at DESC)");

        List<ExistingIndex> indexes = inspector.forTable("orders");

        assertThat(indexes).anySatisfy(index ->
                assertThat(index.columns()).containsExactly("customer_id", "created_at"));
    }
}
