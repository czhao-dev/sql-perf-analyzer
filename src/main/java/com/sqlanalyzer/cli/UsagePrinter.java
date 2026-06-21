package com.sqlanalyzer.cli;

import java.util.Collection;

public final class UsagePrinter {

    private UsagePrinter() {
    }

    public static void printTopLevelUsage() {
        System.out.println("""
                Usage: sql-analyzer <command> [--option=value ...]

                Commands:
                  analyze   Collect slow queries, analyze plans, and generate a report
                  explain   Explain a single query and print findings
                  compare   Compare a before/after EXPLAIN plan pair

                See docs/CLI_USAGE.md for the full option reference.""");
    }

    public static void printUnknownCommand(String command, Collection<String> available) {
        System.err.println("Unknown command: " + command);
        System.err.println("Available commands: " + String.join(", ", available));
    }
}
