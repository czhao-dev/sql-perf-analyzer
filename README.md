# SQL Query Performance Analyzer

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![Tests](https://img.shields.io/badge/tests-35%20passing-brightgreen?logo=junit5&logoColor=white)](#testing-strategy)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A PostgreSQL query performance analysis tool that detects slow queries, collects execution plans, identifies common bottlenecks, and generates optimization reports.

This project is designed to demonstrate practical backend engineering, SQL performance tuning, PostgreSQL internals, query plan analysis, and database observability — implemented as a Java/Spring Boot CLI application.

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
* [x] Sort queries by total execution time
* [x] Filter queries by minimum mean execution time and call count
* [x] Export collected query statistics as JSON

### Query Plan Analysis

* [x] Run `EXPLAIN`
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
* [x] Avoid duplicate recommendations when an equivalent index already exists

### Reports

* [x] Generate Markdown reports
* [x] Generate JSON reports
* [x] Include query text, statistics, plan summary, and recommendations
* [x] Include before/after plan comparison (`compare` command)
* [x] Include risk notes for each recommendation

### Developer Experience

* [x] CLI interface (Spring Boot, `analyze` / `explain` / `compare`)
* [x] Docker Compose demo environment
* [x] Sample PostgreSQL database
* [x] Seed data and intentionally slow queries
* [x] Unit tests (JUnit 5)
* [x] Integration tests (Testcontainers)

## Example Use Cases

This tool can answer questions such as:

* Which queries consume the most total database time?
* Which queries perform sequential scans on large tables?
* Which indexes may improve query performance?
* Which execution plans have poor row-estimation accuracy?
* Did an index improve the query plan after being added?
* Are slow queries caused by scans, joins, sorts, or missing filters?

## Architecture

The codebase is organized as a small pipeline of single-responsibility layers, each its own Java package, wired together with Spring's dependency injection rather than manual `new`-ing or a service locator:

```text
+----------------------+
| CLI (Spring Boot)    |
|----------------------|
| analyze               |
| explain               |
| compare               |
+----------+-----------+
           |
           v
+----------------------+
| Query Collector      |
|----------------------|
| pg_stat_statements   |
| query filters        |
+----------+-----------+
           |
           v
+----------------------+
| Plan Analyzer        |
|----------------------|
| EXPLAIN JSON parser  |
| bottleneck detectors |
| row estimation       |
+----------+-----------+
           |
           v
+----------------------+
| Recommendation Engine|
|----------------------|
| index candidates     |
| existing-index check |
| risk notes           |
+----------+-----------+
           |
           v
+----------------------+
| Report Generator     |
|----------------------|
| Markdown              |
| JSON                  |
| before/after compare  |
+----------------------+
```

### Design decisions worth calling out

* **Pluggable detectors (strategy pattern).** Each bottleneck check (`SeqScanDetector`, `JoinDetector`, `SortDetector`, `RowEstimationDetector`, `HighCostNodeDetector`) implements a single `PlanDetector` interface and is a Spring `@Component`. `DetectorChain` collects every implementation through a constructor-injected `List<PlanDetector>` — adding a new detector is a new class with no registry or switch statement to edit.
* **Immutable domain model.** `PlanNode`, `Finding`, `IndexRecommendation`, `QueryStat`, and `AnalysisResult` are all Java `record`s. The plan tree (`PlanNode`) is a recursive, structurally-shared, side-effect-free value object, which makes the detectors and recommender pure functions over data — easy to unit test with hand-built trees and no mocking.
* **Heuristic recommender, not a SQL parser.** `IndexCandidateExtractor` deliberately uses targeted regular expressions over `EXPLAIN` filter/join/sort text instead of building a SQL AST. This keeps the recommendation logic small and inspectable, at the explicit cost of missing multi-predicate composite indexes — a deliberate tradeoff kept explicit rather than hidden.
* **Config-as-CLI-arguments.** `AnalyzerProperties` is the single source of truth for every tunable (thresholds, output format, database URL), bound from `application.yml`. Spring adds command-line arguments to the `Environment` *before* beans are constructed, so `--analysis.limit=50` overrides the YAML default with zero custom argument-parsing code.
* **Fail-soft per query, not per run.** `pg_stat_statements` normalizes literals to `$1`, `$2`, ... placeholders, which `EXPLAIN` cannot bind. `AnalyzeCommand` catches that specific failure per query and records it as a `Finding` instead of aborting the whole batch — one unexplainable query doesn't take down a 50-query report.
* **Translation boundary for the legacy connection string.** `DatabaseUrlParser` is the one place that understands the `postgres://user:pass@host:port/db` URL shape, isolating that parsing from both the JDBC/Hikari layer and the rest of the app.

## How It Works

```text
1. Connect to PostgreSQL
2. Read query statistics from pg_stat_statements
3. Select candidate queries based on mean/total execution time and call count
4. Run EXPLAIN or EXPLAIN ANALYZE
5. Parse the execution plan
6. Detect bottlenecks
7. Generate index recommendations
8. Write a report
```

## Example Report

See [examples/sample_report.md](examples/sample_report.md) for a real report generated by this tool against the demo dataset. Excerpt:

```text
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
```

## CLI Usage

This is a Spring Boot CLI application, so options use `--key=value` syntax (not space-separated, unlike a typical Go `flag`-style CLI). Config-mirroring options map directly onto `application.yml`'s structure (e.g. `--database.url=...`, `--report.output=...`); command-specific options (`--query=`, `--before=`, `--after=`) are parsed per-command. See [docs/CLI_USAGE.md](docs/CLI_USAGE.md) for the full reference.

### Analyze Slow Queries

```bash
java -jar target/sql-analyzer.jar analyze \
  --database.url=postgres://user:password@localhost:5432/appdb?sslmode=disable \
  --analysis.min-mean-time=50ms \
  --analysis.limit=20 \
  --report.output=report.md
```

### Generate a JSON Report

```bash
java -jar target/sql-analyzer.jar analyze \
  --database.url=postgres://user:password@localhost:5432/appdb?sslmode=disable \
  --report.format=json \
  --report.output=report.json
```

### Analyze a Single Query

```bash
java -jar target/sql-analyzer.jar explain \
  --database.url=postgres://user:password@localhost:5432/appdb?sslmode=disable \
  --query="SELECT * FROM orders WHERE customer_id = 42"
```

### Compare Before and After

```bash
java -jar target/sql-analyzer.jar compare \
  --before=examples/sample_plan.json \
  --after=examples/sample_plan_after.json \
  --report.output=comparison.md
```

## Example Commands

Start the demo Postgres instance:

```bash
make docker-up
```

Run migrations, seed data, and the intentionally slow sample queries:

```bash
make migrate
make seed
make run-slow-queries
```

Analyze the demo database:

```bash
make analyze-demo
```

Run unit tests:

```bash
mvn test
```

Run unit + Testcontainers integration tests:

```bash
mvn verify
```

## Configuration

`application.yml` mirrors this shape (any field is overridable on the command line, e.g. `--analysis.limit=20`):

```yaml
database:
  url: "postgres://postgres:postgres@localhost:5432/demo?sslmode=disable"
  statement-timeout: "5s"

analysis:
  query-source: "pg_stat_statements"
  min-mean-time: "50ms"
  min-calls: 5
  limit: 20
  run-explain-analyze: false
  detect-row-estimation-error: true
  row-estimation-error-threshold: 10.0

recommendations:
  suggest-indexes: true
  check-existing-indexes: true
  include-risk-notes: true

report:
  format: "markdown"
  output: "report.md"
```

## Project Structure

```text
sql-query-performance-analyzer/
├── README.md
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── Makefile
├── src/
│   ├── main/
│   │   ├── java/com/sqlanalyzer/
│   │   │   ├── SqlAnalyzerApplication.java
│   │   │   ├── cli/            # analyze / explain / compare subcommands
│   │   │   ├── config/         # AnalyzerProperties, DataSourceConfig, JsonConfig
│   │   │   ├── db/             # DatabaseUrlParser (postgres:// -> jdbc:postgresql://)
│   │   │   ├── collector/      # pg_stat_statements collection
│   │   │   ├── explain/        # EXPLAIN runner + JSON plan parser
│   │   │   ├── detector/       # pluggable PlanDetector implementations
│   │   │   ├── index/          # index candidate extraction + recommendation
│   │   │   ├── report/         # Markdown/JSON report generation
│   │   │   └── common/         # SqlRedactor
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/   # Flyway: demo schema + pg_stat_statements
│   └── test/
│       ├── java/com/sqlanalyzer/   # unit tests + *IT.java integration tests
│       └── resources/
│           ├── fixtures/       # captured EXPLAIN JSON samples
│           └── examples/
├── seed/
│   └── seed.sql
├── examples/
│   ├── slow_queries.sql
│   ├── sample_report.md
│   ├── sample_report.json
│   ├── sample_plan.json
│   └── sample_plan_after.json
└── docs/
    └── CLI_USAGE.md
```

## PostgreSQL Setup

This project uses PostgreSQL with `pg_stat_statements`. The extension must be **loaded at server start** (not just `CREATE EXTENSION`'d), since it requires `shared_preload_libraries`:

```bash
# postgresql.conf, or as a command-line flag:
postgres -c shared_preload_libraries=pg_stat_statements
```

```sql
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
```

The Docker Compose demo environment and the Testcontainers-based integration tests both configure this automatically.

## Query Plan Analysis

The analyzer parses PostgreSQL JSON plans (`com.sqlanalyzer.explain.PlanJsonParser`) and inspects plan nodes via pluggable detectors (`com.sqlanalyzer.detector`).

Plan node types detected:

```text
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

| Finding | Meaning |
| --- | --- |
| Large sequential scan | Query scans a large table without using an index |
| Expensive sort | Query sorts many rows, possibly needing an index |
| Nested loop over large input | Join may be inefficient for large tables |
| Row-estimation mismatch | Planner estimated far fewer or more rows than actual |
| High-cost plan node | Any plan node whose total cost exceeds a threshold |

## Index Recommendation Logic

The index recommender (`com.sqlanalyzer.index.IndexRecommender`) uses simple text heuristics over plan filter/join/sort expressions — not a full SQL parser. It may recommend indexes when:

* a large table uses a sequential scan with a filter
* a join column (from a hash/merge join condition) lacks an index
* an `ORDER BY` column causes an expensive sort

Recommendations are checked against `pg_indexes` (`com.sqlanalyzer.index.ExistingIndexInspector`) and skipped if an equivalent index already exists.

Important: recommendations are candidates, not automatic changes. Indexes should be reviewed and tested before being applied.

## Safety Considerations

* Uses `EXPLAIN` by default; `EXPLAIN ANALYZE` requires `--analysis.run-explain-analyze=true` (or per-command `--analyze=true`).
* Applies a configurable `statement_timeout` on every connection.
* Does not run destructive SQL or automatically create indexes.
* Best-effort redaction of literal values in reports (`com.sqlanalyzer.common.SqlRedactor`).
* Queries collected from `pg_stat_statements` that contain unresolved `$N` placeholders (most do, since Postgres normalizes literals) cannot be re-run through `EXPLAIN` directly — the `analyze` command reports this per-query as a finding rather than failing the whole run. Use `explain --query="..."` with a concrete literal query for full plan analysis of a specific case.

## Testing Strategy

**Unit tests** (no database, fast): JSON plan parsing, each `PlanDetector`, index candidate extraction and recommendation deduplication, Markdown/JSON report formatting, SQL redaction, config binding.

**Integration tests** (Testcontainers, real PostgreSQL, run via `mvn verify`): query collection, `EXPLAIN`/`EXPLAIN ANALYZE` against real plans, existing-index inspection, and the full `analyze` pipeline end-to-end. All integration test classes share a single Testcontainers Postgres instance (`com.sqlanalyzer.testsupport.AbstractIntegrationTest`) configured with `shared_preload_libraries=pg_stat_statements`.

### Test Results

```text
$ mvn verify

Unit tests (mvn test)
  Tests run: 28, Failures: 0, Errors: 0, Skipped: 0

Integration tests (Testcontainers PostgreSQL, mvn verify)
  Tests run: 7,  Failures: 0, Errors: 0, Skipped: 0

Total: 35 tests, 100% passing
BUILD SUCCESS
```

| Layer | Coverage |
| --- | --- |
| `explain` | JSON plan parsing — flat nodes, nested children, sort keys, real `EXPLAIN ANALYZE` output |
| `detector` | One test class per detector: seq scan, joins, sort, row-estimation mismatch, high-cost nodes |
| `index` | Candidate extraction (filter/join/order-by columns), recommendation dedup against existing indexes |
| `report` | Markdown/JSON rendering, before/after plan comparison deltas |
| `collector` / `cli` | Real `pg_stat_statements` collection and the full `analyze` pipeline against a live, Testcontainers-managed PostgreSQL 16 instance |

### Demo Workloads

The demo schema (`seed/seed.sql`, `examples/slow_queries.sql`) includes intentionally slow query patterns:

* missing index on `orders.customer_id` and `events.customer_id`
* sequential scan on a 100k-row table
* expensive `ORDER BY` without a supporting index
* inefficient hash join across two unindexed foreign keys
* a high-frequency, low-latency query (demonstrates total-time vs mean-time ranking)

## What This Project Demonstrates

* SQL performance tuning
* PostgreSQL query plan analysis
* backend observability
* database metadata inspection
* index design tradeoffs
* Java/Spring Boot CLI development
* structured report generation
* integration testing with Testcontainers and Docker
* performance-focused engineering

## References

* [PostgreSQL EXPLAIN documentation](https://www.postgresql.org/docs/current/sql-explain.html)
* [PostgreSQL `pg_stat_statements` module](https://www.postgresql.org/docs/current/pgstatstatements.html)
* [PostgreSQL query planning overview](https://www.postgresql.org/docs/current/planner-optimizer.html)
* [PostgreSQL `pg_indexes` system catalog](https://www.postgresql.org/docs/current/view-pg-indexes.html)
* [Using EXPLAIN — PostgreSQL wiki](https://wiki.postgresql.org/wiki/Using_EXPLAIN)
* [Slow Query Questions — PostgreSQL wiki](https://wiki.postgresql.org/wiki/Slow_Query_Questions)
* [Testcontainers for Java](https://java.testcontainers.org/)
* [Spring Boot CLI documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
* [Flyway database migrations](https://flywaydb.org/documentation/)

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
