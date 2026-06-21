package com.sqlanalyzer.collector;

import java.time.Duration;

/** Filter/sort criteria applied when collecting rows from {@code pg_stat_statements}. */
public record QueryStatFilter(Duration minMeanTime, int minCalls, int limit) {
}
