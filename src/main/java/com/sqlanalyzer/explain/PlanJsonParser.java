package com.sqlanalyzer.explain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses PostgreSQL's {@code EXPLAIN (FORMAT JSON)} output. Parsed by walking the
 * Jackson tree model directly (rather than POJO binding) since Postgres's JSON
 * keys are Title Case with spaces ("Node Type", "Total Cost", ...).
 */
@Component
public class PlanJsonParser {

    private final ObjectMapper objectMapper;

    public PlanJsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ExplainPlan parse(String json) {
        JsonNode root;
        try {
            root = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("failed to parse EXPLAIN JSON output", e);
        }

        JsonNode planResult = root.isArray() ? root.get(0) : root;
        JsonNode planNode = planResult.get("Plan");
        if (planNode == null) {
            throw new IllegalArgumentException("EXPLAIN JSON output is missing a 'Plan' node");
        }

        return new ExplainPlan(
                parseNode(planNode),
                doubleOrNull(planResult, "Planning Time"),
                doubleOrNull(planResult, "Execution Time"),
                json);
    }

    private PlanNode parseNode(JsonNode node) {
        List<PlanNode> children = new ArrayList<>();
        JsonNode plans = node.get("Plans");
        if (plans != null && plans.isArray()) {
            for (JsonNode child : plans) {
                children.add(parseNode(child));
            }
        }

        return new PlanNode(
                textOrNull(node, "Node Type"),
                textOrNull(node, "Relation Name"),
                textOrNull(node, "Alias"),
                textOrNull(node, "Index Name"),
                doubleOr(node, "Startup Cost", 0),
                doubleOr(node, "Total Cost", 0),
                longOr(node, "Plan Rows", 0),
                intOr(node, "Plan Width", 0),
                longOrNull(node, "Actual Rows"),
                longOrNull(node, "Actual Loops"),
                doubleOrNull(node, "Actual Startup Time"),
                doubleOrNull(node, "Actual Total Time"),
                textOrNull(node, "Join Type"),
                sortKeys(node),
                textOrNull(node, "Filter"),
                textOrNull(node, "Hash Cond"),
                textOrNull(node, "Merge Cond"),
                textOrNull(node, "Index Cond"),
                children);
    }

    private List<String> sortKeys(JsonNode node) {
        JsonNode sortKey = node.get("Sort Key");
        if (sortKey == null || !sortKey.isArray()) {
            return List.of();
        }
        List<String> keys = new ArrayList<>();
        sortKey.forEach(k -> keys.add(k.asText()));
        return List.copyOf(keys);
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static double doubleOr(JsonNode node, String field, double defaultValue) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? defaultValue : value.asDouble();
    }

    private static Double doubleOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asDouble();
    }

    private static long longOr(JsonNode node, String field, long defaultValue) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? defaultValue : value.asLong();
    }

    private static Long longOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asLong();
    }

    private static int intOr(JsonNode node, String field, int defaultValue) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? defaultValue : value.asInt();
    }
}
