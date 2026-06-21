package com.sqlanalyzer.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SqlRedactorTest {

    @Test
    void redactsStringAndNumericLiterals() {
        String redacted = SqlRedactor.redact("SELECT * FROM orders WHERE customer_id = 42 AND status = 'completed'");

        assertThat(redacted).isEqualTo("SELECT * FROM orders WHERE customer_id = ? AND status = '?'");
    }

    @Test
    void leavesAlreadyParameterizedQueriesUnchanged() {
        String redacted = SqlRedactor.redact("SELECT * FROM orders WHERE customer_id = $1");

        assertThat(redacted).isEqualTo("SELECT * FROM orders WHERE customer_id = $1");
    }
}
