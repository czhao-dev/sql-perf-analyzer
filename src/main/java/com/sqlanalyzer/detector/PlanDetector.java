package com.sqlanalyzer.detector;

import com.sqlanalyzer.config.AnalyzerProperties;
import com.sqlanalyzer.explain.ExplainPlan;

import java.util.List;

/** A pluggable bottleneck check run over a parsed plan. Implementations are Spring components. */
public interface PlanDetector {

    List<Finding> detect(ExplainPlan plan, AnalyzerProperties.Analysis thresholds);
}
