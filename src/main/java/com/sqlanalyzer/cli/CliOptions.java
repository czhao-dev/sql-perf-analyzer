package com.sqlanalyzer.cli;

import org.springframework.boot.ApplicationArguments;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helpers for command-specific options (a one-off query string, a file path) that
 * aren't part of the persistent {@code AnalyzerProperties} config schema and so
 * aren't already bound automatically by Spring's {@code --section.field=value} parsing.
 */
public final class CliOptions {

    private static final Pattern DURATION_PATTERN = Pattern.compile("(?i)^(\\d+)(ms|s|m|h)$");

    private CliOptions() {
    }

    public static Optional<String> value(ApplicationArguments args, String name) {
        List<String> values = args.getOptionValues(name);
        return (values == null || values.isEmpty()) ? Optional.empty() : Optional.of(values.get(0));
    }

    public static String requireValue(ApplicationArguments args, String name) {
        return value(args, name)
                .orElseThrow(() -> new IllegalArgumentException("missing required option: --" + name));
    }

    public static boolean flag(ApplicationArguments args, String name, boolean defaultValue) {
        return value(args, name).map(Boolean::parseBoolean).orElse(defaultValue);
    }

    public static Duration parseDuration(String value) {
        Matcher matcher = DURATION_PATTERN.matcher(value.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("invalid duration: " + value + " (expected e.g. 50ms, 5s, 1m)");
        }
        long amount = Long.parseLong(matcher.group(1));
        return switch (matcher.group(2).toLowerCase()) {
            case "ms" -> Duration.ofMillis(amount);
            case "s" -> Duration.ofSeconds(amount);
            case "m" -> Duration.ofMinutes(amount);
            case "h" -> Duration.ofHours(amount);
            default -> throw new IllegalArgumentException("unsupported duration unit in: " + value);
        };
    }
}
