package com.sqlanalyzer.cli;

import com.sqlanalyzer.config.AnalyzerProperties;
import com.sqlanalyzer.detector.DetectorChain;
import com.sqlanalyzer.detector.Finding;
import com.sqlanalyzer.explain.ExplainPlan;
import com.sqlanalyzer.explain.ExplainRunner;
import com.sqlanalyzer.explain.PlanNode;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExplainCommand implements CliCommand {

    private final ExplainRunner explainRunner;
    private final DetectorChain detectorChain;
    private final AnalyzerProperties properties;

    public ExplainCommand(ExplainRunner explainRunner, DetectorChain detectorChain, AnalyzerProperties properties) {
        this.explainRunner = explainRunner;
        this.detectorChain = detectorChain;
        this.properties = properties;
    }

    @Override
    public String name() {
        return "explain";
    }

    @Override
    public int run(ApplicationArguments args) {
        String query = CliOptions.requireValue(args, "query");
        boolean analyze = CliOptions.flag(args, "analyze", properties.analysis().runExplainAnalyze());

        ExplainPlan plan = explainRunner.explain(query, analyze);

        System.out.println("Plan Summary:");
        printNode(plan.root(), 0);
        if (plan.planningTimeMs() != null) {
            System.out.printf("Planning Time: %.3f ms%n", plan.planningTimeMs());
        }
        if (plan.executionTimeMs() != null) {
            System.out.printf("Execution Time: %.3f ms%n", plan.executionTimeMs());
        }

        List<Finding> findings = detectorChain.detect(plan, properties.analysis());
        System.out.println();
        System.out.println("Findings:");
        if (findings.isEmpty()) {
            System.out.println("  (none)");
        } else {
            findings.forEach(f -> System.out.println("  - " + f.message()));
        }

        return 0;
    }

    private void printNode(PlanNode node, int depth) {
        String indent = "  ".repeat(depth);
        StringBuilder line = new StringBuilder(indent).append("- ").append(node.nodeType());
        if (node.relationName() != null) {
            line.append(" on ").append(node.relationName());
        }
        System.out.println(line);
        for (PlanNode child : node.children()) {
            printNode(child, depth + 1);
        }
    }
}
