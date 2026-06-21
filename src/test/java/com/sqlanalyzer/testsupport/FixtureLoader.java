package com.sqlanalyzer.testsupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public final class FixtureLoader {

    private FixtureLoader() {
    }

    public static String load(String fixtureName) {
        try (InputStream in = FixtureLoader.class.getResourceAsStream("/fixtures/" + fixtureName)) {
            if (in == null) {
                throw new IllegalArgumentException("fixture not found: " + fixtureName);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
