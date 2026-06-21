package com.sqlanalyzer.index;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExistingIndexInspectorTest {

    @Test
    void extractsSingleColumnFromIndexDef() {
        var columns = ExistingIndexInspector.extractColumns(
                "CREATE INDEX idx_orders_customer_id ON public.orders USING btree (customer_id)");

        assertThat(columns).containsExactly("customer_id");
    }

    @Test
    void extractsCompositeColumnsAndStripsSortDirection() {
        var columns = ExistingIndexInspector.extractColumns(
                "CREATE INDEX idx_orders_customer_created ON public.orders USING btree (customer_id, created_at DESC)");

        assertThat(columns).containsExactly("customer_id", "created_at");
    }
}
