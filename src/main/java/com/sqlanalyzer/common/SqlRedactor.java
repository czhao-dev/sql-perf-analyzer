package com.sqlanalyzer.common;

import java.util.regex.Pattern;

/**
 * Best-effort redaction of literal values in raw SQL text before it's written into a
 * report, per the README's "redact sensitive query literals" safety guideline.
 * pg_stat_statements already normalizes most repeated-query literals to $N placeholders,
 * so this mainly matters for one-off queries supplied directly via {@code explain --query=}.
 */
public final class SqlRedactor {

    private static final Pattern STRING_LITERAL = Pattern.compile("'(?:[^']|'')*'");
    private static final Pattern NUMERIC_LITERAL = Pattern.compile("(?<=[\\s(,=<>]|^)-?\\d+(\\.\\d+)?(?=[\\s),;]|$)");

    private SqlRedactor() {
    }

    public static String redact(String sql) {
        String withoutStrings = STRING_LITERAL.matcher(sql).replaceAll("'?'");
        return NUMERIC_LITERAL.matcher(withoutStrings).replaceAll("?");
    }
}
