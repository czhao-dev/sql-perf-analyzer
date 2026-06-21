package com.sqlanalyzer.report;

import java.util.List;

public interface ReportGenerator {

    String generateAnalysisReport(List<AnalysisResult> results);

    String generateComparisonReport(ComparisonResult comparison);
}
