package com.sqlanalyzer.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/** Exports raw collected query statistics to JSON, independent of the final analysis report. */
@Component
public class QueryStatExporter {

    private final ObjectMapper objectMapper;

    public QueryStatExporter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(List<QueryStat> stats) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(stats);
        } catch (Exception e) {
            throw new IllegalStateException("failed to export query stats to JSON", e);
        }
    }
}
