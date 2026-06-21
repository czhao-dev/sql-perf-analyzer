package com.sqlanalyzer.cli;

import org.springframework.boot.ApplicationArguments;

/** A single CLI subcommand (analyze, explain, compare). */
public interface CliCommand {

    String name();

    int run(ApplicationArguments args);
}
