package com.sqlanalyzer.detector;

import com.sqlanalyzer.config.AnalyzerProperties;
import com.sqlanalyzer.explain.ExplainPlan;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Aggregates every {@link PlanDetector} bean Spring discovers; add a detector by adding a @Component, no registry edits needed. */
@Component
public class DetectorChain {

    private final List<PlanDetector> detectors;

    public DetectorChain(List<PlanDetector> detectors) {
        this.detectors = detectors;
    }

    public List<Finding> detect(ExplainPlan plan, AnalyzerProperties.Analysis thresholds) {
        List<Finding> findings = new ArrayList<>();
        for (PlanDetector detector : detectors) {
            findings.addAll(detector.detect(plan, thresholds));
        }
        return findings;
    }
}
