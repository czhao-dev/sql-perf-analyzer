package com.sqlanalyzer.collector;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PgStatStatementsCollector {

    private static final String SELECT_SQL = """
            SELECT queryid, query, calls, total_exec_time, mean_exec_time, max_exec_time, rows
            FROM pg_stat_statements
            WHERE mean_exec_time >= ?
              AND calls >= ?
            ORDER BY total_exec_time DESC
            LIMIT ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public PgStatStatementsCollector(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<QueryStat> collect(QueryStatFilter filter) {
        double minMeanTimeMs = filter.minMeanTime() != null ? filter.minMeanTime().toMillis() : 0d;

        return jdbcTemplate.query(SELECT_SQL,
                (rs, rowNum) -> new QueryStat(
                        rs.getLong("queryid"),
                        rs.getString("query"),
                        rs.getLong("calls"),
                        rs.getDouble("total_exec_time"),
                        rs.getDouble("mean_exec_time"),
                        rs.getDouble("max_exec_time"),
                        rs.getLong("rows")),
                minMeanTimeMs, filter.minCalls(), filter.limit());
    }
}
