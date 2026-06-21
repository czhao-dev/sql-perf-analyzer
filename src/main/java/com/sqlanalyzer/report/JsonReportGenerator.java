package com.sqlanalyzer.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JsonReportGenerator implements ReportGenerator {

    private final ObjectMapper objectMapper;

    public JsonReportGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String generateAnalysisReport(List<AnalysisResult> results) {
        return writeJson(results);
    }

    @Override
    public String generateComparisonReport(ComparisonResult comparison) {
        return writeJson(comparison);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("failed to generate JSON report", e);
        }
    }
}
