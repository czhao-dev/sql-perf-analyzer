# SQL Query Performance Report

## Query ID: 5129619640771364291
Mean Time: 18.4 ms
Calls: 10
Total Time: 184.0 ms

Query:
SELECT * FROM orders WHERE customer_id = ? ORDER BY created_at DESC

Plan Summary:
- Sort (estimated 125 rows, actual 25130 rows)
- Seq Scan on orders (estimated 125 rows, actual 25130 rows)

Findings:
- Large sequential scan detected on orders (25130 rows)
- Expensive sort detected on created_at DESC (25130 rows)
- Row estimation mismatch on Seq Scan: estimated 125 rows, actual 25130 rows (201.0x)

Recommendations:
1. Filter column "customer_id" used in a sequential scan on orders:
   CREATE INDEX idx_orders_customer_id ON orders (customer_id);
2. ORDER BY columns [created_at] caused an expensive sort:
   CREATE INDEX idx_orders_created_at ON orders (created_at);

Risk Notes:
- Indexes improve reads but add write overhead.
- Validate with EXPLAIN ANALYZE before applying in production.

---

## Query ID: 3608032995673526740
Mean Time: 2.3 ms
Calls: 10
Total Time: 22.6 ms

Query:
SELECT c.name, COUNT(*) AS event_count
FROM customers c
JOIN events e ON e.customer_id = c.id
GROUP BY c.name

Plan Summary:
- Aggregate
- Hash Join
- Seq Scan on events (estimated 20000 rows, actual 20000 rows)
- Hash
- Seq Scan on customers (estimated 3 rows, actual 3 rows)

Findings:
- Large sequential scan detected on events (20000 rows)
- Hash join detected on (e.customer_id = c.id)

Recommendations:
1. Join column "customer_id" used in Hash Join ((e.customer_id = c.id)):
   CREATE INDEX idx_e_customer_id ON e (customer_id);
2. Join column "id" used in Hash Join ((e.customer_id = c.id)):
   CREATE INDEX idx_c_id ON c (id);

Risk Notes:
- Indexes improve reads but add write overhead.
- Validate with EXPLAIN ANALYZE before applying in production.

---
