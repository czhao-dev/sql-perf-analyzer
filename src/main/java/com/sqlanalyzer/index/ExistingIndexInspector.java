package com.sqlanalyzer.index;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ExistingIndexInspector {

    private static final Pattern COLUMN_LIST_PATTERN = Pattern.compile("\\(([^)]*)\\)");

    private final JdbcTemplate jdbcTemplate;

    public ExistingIndexInspector(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ExistingIndex> forTable(String table) {
        String sql = """
                SELECT schemaname, tablename, indexname, indexdef
                FROM pg_indexes
                WHERE tablename = ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String indexDef = rs.getString("indexdef");
            return new ExistingIndex(
                    rs.getString("schemaname"),
                    rs.getString("tablename"),
                    rs.getString("indexname"),
                    extractColumns(indexDef),
                    indexDef.toUpperCase().contains("CREATE UNIQUE INDEX"),
                    indexDef);
        }, table);
    }

    static List<String> extractColumns(String indexDef) {
        Matcher matcher = COLUMN_LIST_PATTERN.matcher(indexDef);
        if (!matcher.find()) {
            return List.of();
        }
        List<String> columns = new ArrayList<>();
        for (String part : matcher.group(1).split(",")) {
            String column = part.trim().split("\\s+")[0];
            columns.add(column);
        }
        return List.copyOf(columns);
    }
}
