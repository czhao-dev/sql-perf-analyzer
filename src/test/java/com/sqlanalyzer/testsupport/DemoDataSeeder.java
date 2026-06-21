package com.sqlanalyzer.testsupport;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Inserts the same minimal demo rows used by {@code seed/seed.sql}, kept here so
 * integration tests don't depend on shelling out to psql to set up fixture data.
 */
public final class DemoDataSeeder {

    private DemoDataSeeder() {
    }

    public static void seed(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("INSERT INTO customers (name, email) VALUES ('Ada Lovelace', 'ada@example.com')");
        jdbcTemplate.update("INSERT INTO customers (name, email) VALUES ('Grace Hopper', 'grace@example.com')");

        jdbcTemplate.update(
                "INSERT INTO products (name, sku, category, price_cents) VALUES ('Widget', 'SKU-1', 'tools', 1999)");
        jdbcTemplate.update(
                "INSERT INTO products (name, sku, category, price_cents) VALUES ('Gadget', 'SKU-2', 'tools', 2999)");

        for (int i = 0; i < 200; i++) {
            jdbcTemplate.update(
                    "INSERT INTO orders (customer_id, product_id, status, total_cents) VALUES (1, 1, 'completed', 1999)");
        }
    }
}
