# CLI Usage

`sql-analyzer` is a Spring Boot CLI application. It dispatches on the first
non-option argument as the subcommand (`analyze`, `explain`, or `compare`),
and reads everything else through Spring's `ApplicationArguments`.

## Syntax change from the original Go design

The Go version of this tool used space-separated flags, e.g. `--min-mean-time 50ms`.
Spring Boot's built-in command-line argument parser only understands
`--key=value` syntax (or a bare `--key` as a boolean flag) — it does not pair
a flag with a following, separate argument. So every option below uses `=`.

## Two kinds of options

**Config-mirroring options** map directly onto `AnalyzerProperties`
(`src/main/java/com/sqlanalyzer/config/AnalyzerProperties.java`), which also
backs `application.yml`. Because Spring adds command-line arguments to the
`Environment` before beans are created, these are *not* parsed by any
command-specific code — passing `--database.url=...` on the command line
transparently overrides the bound property for that run. They follow the
`--<section>.<field>=<value>` shape, matching `application.yml`'s structure:

| Option | Default | Notes |
| --- | --- | --- |
| `--database.url=...` | `postgres://postgres:postgres@localhost:5432/demo?sslmode=disable` | accepts the Go-style `postgres://` URL or a plain `jdbc:postgresql://` URL |
| `--database.statement-timeout=...` | `5s` | applied via `SET statement_timeout` on each connection |
| `--analysis.min-mean-time=...` | `50ms` | minimum mean execution time to collect |
| `--analysis.min-calls=...` | `5` | minimum call count to collect |
| `--analysis.limit=...` | `20` | max queries collected/analyzed |
| `--analysis.run-explain-analyze=...` | `false` | runs `EXPLAIN ANALYZE` (executes the query) instead of plain `EXPLAIN` |
| `--analysis.detect-row-estimation-error=...` | `true` | toggles the row-estimation mismatch detector |
| `--analysis.row-estimation-error-threshold=...` | `10.0` | minimum estimate/actual ratio to flag |
| `--recommendations.suggest-indexes=...` | `true` | toggles the index recommender |
| `--recommendations.check-existing-indexes=...` | `true` | dedupes recommendations against `pg_indexes` |
| `--recommendations.include-risk-notes=...` | `true` | toggles general risk notes in the report |
| `--report.format=...` | `markdown` | `markdown` or `json` |
| `--report.output=...` | `report.md` | output file path; omit value or leave blank to print to stdout |

**Command-specific options** aren't part of the persistent config and are
parsed directly by each `CliCommand` (`src/main/java/com/sqlanalyzer/cli/`):

| Command | Option | Required | Notes |
| --- | --- | --- | --- |
| `explain` | `--query=...` | yes | the SQL to explain |
| `explain` | `--analyze=true\|false` | no | defaults to `analysis.run-explain-analyze` |
| `compare` | `--before=...` | yes | path to a captured `EXPLAIN (FORMAT JSON)` file |
| `compare` | `--after=...` | yes | path to a captured `EXPLAIN (FORMAT JSON)` file |

## Examples

Analyze slow queries and write a Markdown report:

```bash
java -jar target/sql-analyzer.jar analyze \
  --database.url=postgres://user:password@localhost:5432/appdb?sslmode=disable \
  --analysis.min-mean-time=50ms \
  --analysis.limit=20 \
  --report.output=report.md
```

Generate a JSON report instead:

```bash
java -jar target/sql-analyzer.jar analyze \
  --database.url=postgres://user:password@localhost:5432/appdb?sslmode=disable \
  --report.format=json \
  --report.output=report.json
```

Explain a single query:

```bash
java -jar target/sql-analyzer.jar explain \
  --database.url=postgres://user:password@localhost:5432/appdb?sslmode=disable \
  --query="SELECT * FROM orders WHERE customer_id = 42"
```

Compare a before/after plan pair:

```bash
java -jar target/sql-analyzer.jar compare \
  --before=examples/sample_plan.json \
  --after=examples/sample_plan_after.json \
  --report.output=comparison.md
```
