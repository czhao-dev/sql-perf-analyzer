-- Intentionally slow queries that populate pg_stat_statements with realistic
-- bottlenecks for the demo: a missing-index sequential scan, an expensive
-- ORDER BY, and an inefficient join. Run via `make run-slow-queries`.

-- Sequential scan: no index on orders.customer_id.
SELECT * FROM orders WHERE customer_id = 1;

-- Expensive ORDER BY: sorts the full filtered result set without a supporting index.
SELECT * FROM orders WHERE customer_id = 2 ORDER BY created_at DESC;

-- Inefficient join: no index on events.customer_id either.
SELECT c.name, COUNT(*) AS event_count
FROM customers c
JOIN events e ON e.customer_id = c.id
GROUP BY c.name;

-- High-frequency small query: cheap per call, but demonstrates "high total
-- database impact despite moderate average latency" when called often.
SELECT id FROM products WHERE sku = 'SKU-1001';
