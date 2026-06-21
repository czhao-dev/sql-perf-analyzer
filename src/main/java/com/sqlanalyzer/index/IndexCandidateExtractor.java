package com.sqlanalyzer.index;

import com.sqlanalyzer.explain.PlanNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pulls index candidates out of a plan tree using simple text heuristics over
 * filter/join/sort expressions, matching the README's "simple heuristics" design
 * (not a full SQL expression parser).
 */
class IndexCandidateExtractor {

    private static final Pattern FILTER_COLUMN_PATTERN =
            Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\.([a-zA-Z_][a-zA-Z0-9_]*)|([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?:=|<=|>=|<>|<|>|LIKE)",
                    Pattern.CASE_INSENSITIVE);
    private static final Pattern QUALIFIED_COLUMN_PATTERN =
            Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\.([a-zA-Z_][a-zA-Z0-9_]*)");
    private static final Pattern SORT_DIRECTION_PATTERN =
            Pattern.compile("(?i)\\s+(DESC|ASC)(\\s+NULLS\\s+(FIRST|LAST))?\\s*$");

    List<IndexCandidate> extract(PlanNode root) {
        List<IndexCandidate> candidates = new ArrayList<>();
        for (PlanNode node : root.flatten()) {
            extractFilterCandidate(node).ifPresent(candidates::add);
            candidates.addAll(extractJoinCandidates(node));
            extractOrderByCandidate(node).ifPresent(candidates::add);
        }
        return candidates;
    }

    private Optional<IndexCandidate> extractFilterCandidate(PlanNode node) {
        if (node.filter() == null || node.relationName() == null) {
            return Optional.empty();
        }
        String column = extractColumn(node.filter());
        if (column == null) {
            return Optional.empty();
        }
        return Optional.of(new IndexCandidate(
                node.relationName(),
                List.of(column),
                IndexRecommendationKind.FILTER,
                "Filter column \"" + column + "\" used in a sequential scan on " + node.relationName()));
    }

    private List<IndexCandidate> extractJoinCandidates(PlanNode node) {
        String cond = node.hashCond() != null ? node.hashCond() : node.mergeCond();
        if (cond == null) {
            return List.of();
        }
        List<IndexCandidate> candidates = new ArrayList<>();
        Matcher matcher = QUALIFIED_COLUMN_PATTERN.matcher(cond);
        while (matcher.find()) {
            String table = matcher.group(1);
            String column = matcher.group(2);
            candidates.add(new IndexCandidate(
                    table,
                    List.of(column),
                    IndexRecommendationKind.JOIN,
                    "Join column \"" + column + "\" used in " + node.nodeType() + " (" + cond + ")"));
        }
        return candidates;
    }

    private Optional<IndexCandidate> extractOrderByCandidate(PlanNode node) {
        if (node.sortKey().isEmpty()) {
            return Optional.empty();
        }
        List<String> columns = node.sortKey().stream().map(this::stripDirection).toList();
        String table = inferTableFromSortNode(node);
        if (table == null) {
            return Optional.empty();
        }
        return Optional.of(new IndexCandidate(
                table,
                columns,
                IndexRecommendationKind.ORDER_BY,
                "ORDER BY columns " + columns + " caused an expensive sort"));
    }

    private String extractColumn(String expression) {
        Matcher matcher = FILTER_COLUMN_PATTERN.matcher(expression);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
    }

    private String stripDirection(String sortKey) {
        String withoutDirection = SORT_DIRECTION_PATTERN.matcher(sortKey).replaceAll("").trim();
        int dotIndex = withoutDirection.lastIndexOf('.');
        return dotIndex >= 0 ? withoutDirection.substring(dotIndex + 1) : withoutDirection;
    }

    private String inferTableFromSortNode(PlanNode sortNode) {
        String firstSortKey = sortNode.sortKey().get(0);
        int dotIndex = firstSortKey.indexOf('.');
        if (dotIndex > 0) {
            return firstSortKey.substring(0, dotIndex);
        }
        for (PlanNode descendant : sortNode.flatten()) {
            if (descendant.relationName() != null) {
                return descendant.relationName();
            }
        }
        return null;
    }
}
