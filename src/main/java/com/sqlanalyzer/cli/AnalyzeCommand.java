package com.sqlanalyzer.cli;

import com.sqlanalyzer.collector.PgStatStatementsCollector;
import com.sqlanalyzer.collector.QueryStat;
import com.sqlanalyzer.collector.QueryStatFilter;
import com.sqlanalyzer.config.AnalyzerProperties;
import com.sqlanalyzer.detector.DetectorChain;
import com.sqlanalyzer.detector.Finding;
import com.sqlanalyzer.detector.FindingCategory;
import com.sqlanalyzer.detector.Severity;
import com.sqlanalyzer.explain.ExplainPlan;
import com.sqlanalyzer.explain.ExplainRunner;
import com.sqlanalyzer.index.IndexRecommendation;
import com.sqlanalyzer.index.IndexRecommender;
import com.sqlanalyzer.report.AnalysisResult;
import com.sqlanalyzer.report.JsonReportGenerator;
import com.sqlanalyzer.report.MarkdownReportGenerator;
import com.sqlanalyzer.report.ReportGenerator;
import com.sqlanalyzer.report.ReportWriter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AnalyzeCommand implements CliCommand {

    private final PgStatStatementsCollector collector;
    private final ExplainRunner explainRunner;
    private final DetectorChain detectorChain;
    private final IndexRecommender indexRecommender;
    private final MarkdownReportGenerator markdownReportGenerator;
    private final JsonReportGenerator jsonReportGenerator;
    private final ReportWriter reportWriter;
    private final AnalyzerProperties properties;

    public AnalyzeCommand(PgStatStatementsCollector collector, ExplainRunner explainRunner, DetectorChain detectorChain,
            IndexRecommender indexRecommender, MarkdownReportGenerator markdownReportGenerator,
            JsonReportGenerator jsonReportGenerator, ReportWriter reportWriter, AnalyzerProperties properties) {
        this.collector = collector;
        this.explainRunner = explainRunner;
        this.detectorChain = detectorChain;
        this.indexRecommender = indexRecommender;
        this.markdownReportGenerator = markdownReportGenerator;
        this.jsonReportGenerator = jsonReportGenerator;
        this.reportWriter = reportWriter;
        this.properties = properties;
    }

    @Override
    public String name() {
        return "analyze";
    }

    @Override
    public int run(ApplicationArguments args) {
        AnalyzerProperties.Analysis analysis = properties.analysis();
        QueryStatFilter filter = new QueryStatFilter(analysis.minMeanTime(), analysis.minCalls(), analysis.limit());

        List<QueryStat> stats = collector.collect(filter);
        List<AnalysisResult> results = new ArrayList<>();
        for (QueryStat stat : stats) {
            results.add(analyze(stat, analysis));
        }

        ReportGenerator reportGenerator = "json".equalsIgnoreCase(properties.report().format())
                ? jsonReportGenerator
                : markdownReportGenerator;
        reportWriter.write(reportGenerator.generateAnalysisReport(results), properties.report().output());

        System.out.printf("Analyzed %d quer%s, report written to %s%n",
                results.size(), results.size() == 1 ? "y" : "ies", properties.report().output());
        return 0;
    }

    private AnalysisResult analyze(QueryStat stat, AnalyzerProperties.Analysis analysis) {
        try {
            ExplainPlan plan = explainRunner.explain(stat.query(), analysis.runExplainAnalyze());
            List<Finding> findings = detectorChain.detect(plan, analysis);
            List<IndexRecommendation> recommendations = properties.recommendations().suggestIndexes()
                    ? indexRecommender.recommend(plan.root())
                    : List.of();
            List<String> riskNotes = riskNotes();
            return new AnalysisResult(stat, plan, findings, recommendations, riskNotes);
        } catch (DataAccessException e) {
            Finding failure = new Finding(
                    FindingCategory.EXPLAIN_FAILED,
                    Severity.INFO,
                    "Could not run EXPLAIN on this query, likely due to unresolved parameter placeholders ($1, $2, ...) "
                            + "in the text collected from pg_stat_statements: " + e.getMostSpecificCause().getMessage(),
                    null,
                    null);
            return new AnalysisResult(stat, null, List.of(failure), List.of(), List.of());
        }
    }

    private List<String> riskNotes() {
        if (!properties.recommendations().includeRiskNotes()) {
            return List.of();
        }
        return List.of(
                "Indexes improve reads but add write overhead.",
                "Validate with EXPLAIN ANALYZE before applying in production.");
    }
}
