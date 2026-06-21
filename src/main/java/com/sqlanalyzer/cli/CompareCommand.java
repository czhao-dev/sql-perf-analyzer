package com.sqlanalyzer.cli;

import com.sqlanalyzer.config.AnalyzerProperties;
import com.sqlanalyzer.explain.ExplainPlan;
import com.sqlanalyzer.explain.PlanJsonParser;
import com.sqlanalyzer.report.ComparisonResult;
import com.sqlanalyzer.report.JsonReportGenerator;
import com.sqlanalyzer.report.MarkdownReportGenerator;
import com.sqlanalyzer.report.ReportGenerator;
import com.sqlanalyzer.report.ReportWriter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class CompareCommand implements CliCommand {

    private final PlanJsonParser planJsonParser;
    private final MarkdownReportGenerator markdownReportGenerator;
    private final JsonReportGenerator jsonReportGenerator;
    private final ReportWriter reportWriter;
    private final AnalyzerProperties properties;

    public CompareCommand(PlanJsonParser planJsonParser, MarkdownReportGenerator markdownReportGenerator,
            JsonReportGenerator jsonReportGenerator, ReportWriter reportWriter, AnalyzerProperties properties) {
        this.planJsonParser = planJsonParser;
        this.markdownReportGenerator = markdownReportGenerator;
        this.jsonReportGenerator = jsonReportGenerator;
        this.reportWriter = reportWriter;
        this.properties = properties;
    }

    @Override
    public String name() {
        return "compare";
    }

    @Override
    public int run(ApplicationArguments args) {
        String beforePath = CliOptions.requireValue(args, "before");
        String afterPath = CliOptions.requireValue(args, "after");

        ExplainPlan before = planJsonParser.parse(readFile(beforePath));
        ExplainPlan after = planJsonParser.parse(readFile(afterPath));
        ComparisonResult comparison = ComparisonResult.of(before, after);

        ReportGenerator reportGenerator = "json".equalsIgnoreCase(properties.report().format())
                ? jsonReportGenerator
                : markdownReportGenerator;
        reportWriter.write(reportGenerator.generateComparisonReport(comparison), properties.report().output());

        System.out.println("Comparison report written to " + properties.report().output());
        return 0;
    }

    private String readFile(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            throw new UncheckedIOException("failed to read plan file: " + path, e);
        }
    }
}
