package com.sqlanalyzer.explain;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ExplainRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PlanJsonParser planJsonParser;

    public ExplainRunner(JdbcTemplate jdbcTemplate, PlanJsonParser planJsonParser) {
        this.jdbcTemplate = jdbcTemplate;
        this.planJsonParser = planJsonParser;
    }

    /**
     * Runs {@code EXPLAIN (FORMAT JSON)} or, if {@code analyze} is true,
     * {@code EXPLAIN (ANALYZE, FORMAT JSON)} (which executes the query) on the given SQL.
     */
    public ExplainPlan explain(String query, boolean analyze) {
        String options = analyze ? "ANALYZE, FORMAT JSON" : "FORMAT JSON";
        String json = jdbcTemplate.queryForObject("EXPLAIN (" + options + ") " + query, String.class);
        return planJsonParser.parse(json);
    }
}
