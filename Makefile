DATABASE_URL ?= postgres://postgres:postgres@localhost:5432/demo?sslmode=disable
JDBC_URL := $(subst postgres://,jdbc:postgresql://,$(DATABASE_URL))
JAR := target/sql-analyzer.jar

.PHONY: build test verify migrate seed run-slow-queries analyze-demo docker-up docker-down

build:
	mvn -B package -DskipTests

test:
	mvn -B test

verify:
	mvn -B verify

migrate:
	mvn -B flyway:migrate -Dflyway.url="$(JDBC_URL)" -Dflyway.user=postgres -Dflyway.password=postgres

seed:
	psql "$(DATABASE_URL)" -f seed/seed.sql

run-slow-queries:
	psql "$(DATABASE_URL)" -f examples/slow_queries.sql

analyze-demo: build
	java -jar $(JAR) analyze --database.url="$(DATABASE_URL)" --report.output=report.md

docker-up:
	docker compose up -d postgres

docker-down:
	docker compose down -v
