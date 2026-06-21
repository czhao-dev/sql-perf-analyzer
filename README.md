# SQL Query Performance Analyzer

A PostgreSQL query performance analysis tool that detects slow queries, collects execution plans, identifies common bottlenecks, and generates optimization reports.

This project is designed to demonstrate practical backend engineering, SQL performance tuning, PostgreSQL internals, query plan analysis, and database observability.

## Overview

`sql-query-performance-analyzer` connects to a PostgreSQL database, collects query statistics, runs query plan analysis, and produces actionable performance reports.

The tool can help identify:

* slow queries
* expensive sequential scans
* missing or unused indexes
* inefficient joins
* costly sorts
* high planning or execution time
* large row-estimation errors
* queries with high total database impact
* candidate indexes for optimization

The goal is not to replace professional database monitoring platforms. Instead, this project implements the core ideas behind SQL query observability and performance analysis in a compact, readable backend tool.

## Motivation

SQL performance is a common bottleneck in backend systems. A single inefficient query can increase latency, consume CPU and memory, lock database resources, and degrade the entire service.

This project explores important database and backend concepts:

* SQL query execution plans
* PostgreSQL `EXPLAIN` and `EXPLAIN ANALYZE`
* index selection
* table statistics
* query latency analysis
* row-estimation accuracy
* backend observability
* report generation
* safe database inspection

## Features

### Query Collection

* [x] Connect to PostgreSQL
* [x] Collect slow queries from `pg_stat_statements`
* [x] Sort queries by mean execution time
* [x] Sort queries by total execution time
* [x] Filter queries by database, user, or minimum latency
* [x] Export collected query statistics

### Query Plan Analysis

* [x] Run `EXPLAIN` on selected queries
* [x] Run `EXPLAIN ANALYZE`, optional and configurable
* [x] Parse JSON execution plans
* [x] Detect sequential scans
* [x] Detect nested loop joins
* [x] Detect expensive sorts
* [x] Detect hash joins and merge joins
* [x] Detect row-estimation mismatch
* [x] Detect high-cost plan nodes

### Index Recommendations

* [x] Suggest candidate indexes for large sequential scans
* [x] Detect filters that may benefit from indexing
* [x] Detect join columns that may benefit from indexing
* [x] Detect order-by columns that may benefit from indexing
* [x] Avoid duplicate recommendations when similar indexes already exist

### Reports

* [x] Generate Markdown reports
* [x] Generate JSON reports
* [x] Include query text, statistics, plan summary, and recommendations
* [x] Include before/after comparison, optional
* [x] Include risk notes for each recommendation

### Developer Experience

* [x] CLI interface
* [x] Docker Compose demo environment
* [x] Sample PostgreSQL database
* [x] Seed data and intentionally slow queries
* [x] Unit tests
* [x] Integration tests
* [x] Benchmark scripts

Update this section as implementation progresses.

## Example Use Cases

This tool can answer questions such as:

* Which queries have the highest average latency?
* Which queries consume the most total database time?
* Which queries perform sequential scans on large tables?
* Which indexes may improve query performance?
* Which execution plans have poor row-estimation accuracy?
* Did an index improve the query plan after being added?
* Are slow queries caused by scans, joins, sorts, or missing filters?

## Architecture

```text id="f4ivf4"
+----------------------+
| CLI / API            |
|----------------------|
| analyze              |
| report               |
| compare              |
+----------+-----------+
           |
           v
+----------------------+
| Query Collector      |
|----------------------|
| pg_stat_statements   |
| query filters        |
| query ranking        |
+----------+-----------+
           |
           v
+----------------------+
| Plan Analyzer        |
|----------------------|
| EXPLAIN JSON         |
| plan node parser     |
| cost analysis        |
| row estimation       |
+----------+-----------+
           |
           v
+----------------------+
| Recommendation Engine|
|----------------------|
| index candidates     |
| bottleneck detection |
| risk notes           |
+----------+-----------+
           |
           v
+----------------------+
| Report Generator     |
|----------------------|
| Markdown             |
| JSON                 |
| before/after diff    |
+----------------------+
```

## How It Works

The analyzer follows this workflow:

```text id="96fdfk"
1. Connect to PostgreSQL
2. Read query statistics from pg_stat_statements
3. Select candidate queries based on latency or total cost
4. Run EXPLAIN or EXPLAIN ANALYZE
5. Parse the execution plan
6. Detect bottlenecks
7. Generate recommendations
8. Write a report
```

## Example Report

```text id="c0ej18"
Query ID: 872193
Mean Time: 184.2 ms
Calls: 1520
Total Time: 280064 ms

Query:
SELECT * FROM orders WHERE customer_id = $1 ORDER BY created_at DESC;

Plan Summary:
- Sequential Scan on orders
- Sort on created_at
- Estimated rows: 125
- Actual rows: 5820
- Row estimate error: 46.5x

Findings:
- Large sequential scan detected on orders
- Sort operation detected after filtering
- Row estimation mismatch detected

Recommendations:
1. Consider a composite index:
   CREATE INDEX idx_orders_customer_created
   ON orders (customer_id, created_at DESC);

2. Run ANALYZE on the table if statistics are stale:
   ANALYZE orders;

Risk Notes:
- Indexes improve reads but add write overhead.
- Validate with EXPLAIN ANALYZE before applying in production.
```

## CLI Usage

### Analyze Slow Queries

```bash id="nj0ewh"
sql-analyzer analyze \
  --database-url "postgres://user:password@localhost:5432/appdb?sslmode=disable" \
  --min-mean-time 50ms \
  --limit 20 \
  --output report.md
```

### Generate JSON Report

```bash id="hqn6y8"
sql-analyzer analyze \
  --database-url "postgres://user:password@localhost:5432/appdb?sslmode=disable" \
  --format json \
  --output report.json
```

### Analyze a Single Query

```bash id="5160uw"
sql-analyzer explain \
  --database-url "postgres://user:password@localhost:5432/appdb?sslmode=disable" \
  --query "SELECT * FROM orders WHERE customer_id = 42"
```

### Compare Before and After

```bash id="9a5zfu"
sql-analyzer compare \
  --before before-plan.json \
  --after after-plan.json \
  --output comparison.md
```

## Example Commands

Start the demo environment:

```bash id="ejcyr8"
docker compose up --build
```

Run migrations and seed data:

```bash id="ie6gya"
make migrate
make seed
```

Run intentionally slow sample queries:

```bash id="1cnhy7"
make run-slow-queries
```

Analyze the demo database:

```bash id="wido3u"
make analyze-demo
```

Run tests:

```bash id="5jszbf"
go test ./...
```

Run linting:

```bash id="vn4cg7"
go fmt ./...
go vet ./...
```

## Configuration

Example `config.yaml`:

```yaml id="z9vx5s"
database:
  url: "postgres://postgres:postgres@localhost:5432/demo?sslmode=disable"
  statement_timeout: "5s"

analysis:
  query_source: "pg_stat_statements"
  min_mean_time: "50ms"
  min_calls: 5
  limit: 20
  run_explain_analyze: false
  detect_row_estimation_error: true
  row_estimation_error_threshold: 10.0

recommendations:
  suggest_indexes: true
  check_existing_indexes: true
  include_risk_notes: true

report:
  format: "markdown"
  output: "report.md"
```

## Project Structure

Suggested layout:

```text id="7h4bq4"
sql-query-performance-analyzer/
├── README.md
├── go.mod
├── go.sum
├── Dockerfile
├── docker-compose.yml
├── Makefile
├── configs/
│   └── config.yaml
├── cmd/
│   └── sql-analyzer/
│       └── main.go
├── internal/
│   ├── collector/
│   │   ├── pg_stat_statements.go
│   │   └── query_stats.go
│   ├── explain/
│   │   ├── runner.go
│   │   ├── parser.go
│   │   └── plan_node.go
│   ├── analyzer/
│   │   ├── scan_detector.go
│   │   ├── join_detector.go
│   │   ├── sort_detector.go
│   │   └── row_estimation.go
│   ├── index/
│   │   ├── recommender.go
│   │   └── existing_indexes.go
│   ├── report/
│   │   ├── markdown.go
│   │   └── json.go
│   ├── config/
│   │   └── config.go
│   └── db/
│       └── postgres.go
├── migrations/
│   ├── 001_create_demo_schema.sql
│   └── 002_enable_pg_stat_statements.sql
├── seed/
│   └── seed.sql
├── examples/
│   ├── slow_queries.sql
│   ├── sample_report.md
│   └── sample_plan.json
└── tests/
    ├── integration/
    └── fixtures/
```

## PostgreSQL Setup

This project uses PostgreSQL and optionally `pg_stat_statements`.

Enable the extension:

```sql id="zzdrq5"
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
```

Example query for collecting slow statements:

```sql id="1e1fkh"
SELECT
    queryid,
    query,
    calls,
    total_exec_time,
    mean_exec_time,
    max_exec_time,
    rows
FROM pg_stat_statements
ORDER BY total_exec_time DESC
LIMIT 20;
```

## Query Plan Analysis

The analyzer parses PostgreSQL JSON plans and inspects plan nodes.

Plan node types detected:

```text id="p83m0t"
Seq Scan
Index Scan
Index Only Scan
Bitmap Heap Scan
Nested Loop
Hash Join
Merge Join
Sort
Aggregate
Limit
Gather
```

Example findings:

| Finding                      | Meaning                                                   |
| ---------------------------- | --------------------------------------------------------- |
| Large sequential scan        | Query scans a large table without using an index          |
| Expensive sort               | Query sorts many rows, possibly needing an index          |
| Nested loop over large input | Join may be inefficient for large tables                  |
| Row-estimation mismatch      | Planner estimated far fewer or more rows than actual      |
| High total query time        | Query may be frequent even if average latency is moderate |

## Index Recommendation Logic

The index recommender uses simple heuristics based on query plans and table metadata.

It may recommend indexes when:

* a large table uses sequential scan
* a filter column is repeatedly used in slow queries
* a join column lacks an index
* an `ORDER BY` column causes an expensive sort
* a composite index may support both filtering and sorting

Example recommendation:

```sql id="2s97jv"
CREATE INDEX idx_orders_customer_id_created_at
ON orders (customer_id, created_at DESC);
```

Important: recommendations are candidates, not automatic changes. Indexes should be reviewed and tested before being applied.

## Safety Considerations

This tool is designed to inspect and analyze queries safely.

Recommended safety defaults:

* Use `EXPLAIN` by default.
* Require an explicit flag for `EXPLAIN ANALYZE`.
* Set a statement timeout.
* Do not run destructive SQL.
* Do not automatically create indexes.
* Redact sensitive query literals in reports when possible.
* Run against staging or read replicas when analyzing production-like workloads.

## Testing Strategy

### Unit Tests

Unit tests cover:

* JSON plan parsing
* sequential scan detection
* join detection
* sort detection
* row-estimation error calculation
* index recommendation generation
* Markdown and JSON report formatting
* configuration parsing

### Integration Tests

Integration tests cover:

* connecting to PostgreSQL
* enabling demo schema
* collecting query statistics
* running `EXPLAIN`
* parsing real PostgreSQL plans
* generating reports from real queries

### Demo Workloads

The demo database includes intentionally slow query patterns:

* missing index on foreign key
* sequential scan on large table
* expensive `ORDER BY`
* inefficient join
* stale statistics simulation
* high-frequency small query

## Benchmark Plan

Benchmark the analyzer itself:

```text id="vtoxlg"
plan parsing throughput
recommendation generation time
report generation time
database collection latency
```

Benchmark query improvements:

```text id="bt6tll"
before index: sequential scan + sort
after index:  index scan using composite index
```

Example benchmark table:

| Query              | Before | After | Improvement |
| ------------------ | -----: | ----: | ----------: |
| Orders by customer | 184 ms | 12 ms |       15.3x |
| Product search     |  96 ms | 18 ms |        5.3x |
| Recent events      | 240 ms | 31 ms |        7.7x |

## Roadmap

### Phase 1: Basic Collector

* [ ] Connect to PostgreSQL
* [ ] Collect slow queries from `pg_stat_statements`
* [ ] Sort by mean and total execution time
* [ ] Export raw query stats

### Phase 2: Plan Analyzer

* [ ] Run `EXPLAIN (FORMAT JSON)`
* [ ] Parse plan nodes
* [ ] Detect sequential scans
* [ ] Detect expensive joins
* [ ] Detect expensive sorts
* [ ] Detect row-estimation mismatch

### Phase 3: Recommendation Engine

* [ ] Inspect existing indexes
* [ ] Suggest candidate indexes
* [ ] Add recommendation confidence levels
* [ ] Add risk notes

### Phase 4: Reports

* [ ] Generate Markdown reports
* [ ] Generate JSON reports
* [ ] Add before/after comparison
* [ ] Add query summary tables

### Phase 5: Polish

* [ ] Add Docker Compose demo
* [ ] Add integration tests
* [ ] Add CI workflow
* [ ] Add sample reports
* [ ] Add benchmark results

## What This Project Demonstrates

This project demonstrates:

* SQL performance tuning
* PostgreSQL query plan analysis
* backend observability
* database metadata inspection
* index design tradeoffs
* Go CLI/backend development
* structured report generation
* integration testing with Docker
* performance-focused engineering

## Non-Goals

This project does not aim to be a full database monitoring platform.

Non-goals:

* automatic query rewriting
* automatic index creation in production
* support for every SQL dialect
* complete PostgreSQL planner emulation
* replacing commercial APM/database tools
* making production changes without human review

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
