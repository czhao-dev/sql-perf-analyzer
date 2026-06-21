package com.sqlanalyzer.collector;

import com.sqlanalyzer.testsupport.AbstractIntegrationTest;
import com.sqlanalyzer.testsupport.DemoDataSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PgStatStatementsCollectorIT extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PgStatStatementsCollector collector;

    @BeforeEach
    void seedAndWarmStatements() {
        jdbcTemplate.execute("SELECT pg_stat_statements_reset()");
        DemoDataSeeder.seed(jdbcTemplate);

        for (int i = 0; i < 10; i++) {
            jdbcTemplate.queryForList("SELECT * FROM orders WHERE customer_id = 1");
        }
    }

    @Test
    void collectsQueryStatsAboveThresholds() {
        QueryStatFilter filter = new QueryStatFilter(Duration.ZERO, 1, 20);

        List<QueryStat> stats = collector.collect(filter);

        assertThat(stats).isNotEmpty();
        assertThat(stats)
                .anySatisfy(stat -> assertThat(stat.query()).containsIgnoringCase("orders"));
        assertThat(stats).allSatisfy(stat -> assertThat(stat.calls()).isGreaterThanOrEqualTo(1));
    }

    @Test
    void filtersOutQueriesBelowMinCallsThreshold() {
        QueryStatFilter filter = new QueryStatFilter(Duration.ZERO, 1_000_000, 20);

        List<QueryStat> stats = collector.collect(filter);

        assertThat(stats).isEmpty();
    }
}
