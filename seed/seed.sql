-- Seed data for the demo schema, including enough rows that the
-- intentionally-missing index on orders.customer_id (see
-- src/main/resources/db/migration/V1__create_demo_schema.sql) produces a
-- measurable sequential scan when queried.

INSERT INTO customers (name, email) VALUES
    ('Ada Lovelace', 'ada@example.com'),
    ('Grace Hopper', 'grace@example.com'),
    ('Alan Turing', 'alan@example.com');

INSERT INTO products (name, sku, category, price_cents) VALUES
    ('Widget', 'SKU-1001', 'tools', 1999),
    ('Gadget', 'SKU-1002', 'tools', 2999),
    ('Gizmo', 'SKU-1003', 'electronics', 4999);

INSERT INTO orders (customer_id, product_id, status, total_cents, created_at)
SELECT
    (random() * 2 + 1)::bigint,
    (random() * 2 + 1)::bigint,
    (ARRAY['pending', 'completed', 'cancelled'])[floor(random() * 3 + 1)],
    (random() * 10000)::bigint,
    now() - (random() * interval '365 days')
FROM generate_series(1, 100000);

INSERT INTO events (customer_id, event_type, payload, created_at)
SELECT
    (random() * 2 + 1)::bigint,
    (ARRAY['login', 'page_view', 'purchase'])[floor(random() * 3 + 1)],
    jsonb_build_object('source', 'seed'),
    now() - (random() * interval '90 days')
FROM generate_series(1, 20000);

ANALYZE customers;
ANALYZE products;
ANALYZE orders;
ANALYZE events;
