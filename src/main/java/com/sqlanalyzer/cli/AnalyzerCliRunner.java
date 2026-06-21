package com.sqlanalyzer.cli;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AnalyzerCliRunner implements ApplicationRunner {

    private final Map<String, CliCommand> commandsByName;
    private int exitCode = 0;

    public AnalyzerCliRunner(List<CliCommand> commands) {
        this.commandsByName = commands.stream().collect(Collectors.toMap(CliCommand::name, c -> c));
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> nonOptionArgs = args.getNonOptionArgs();
        if (nonOptionArgs.isEmpty()) {
            UsagePrinter.printTopLevelUsage();
            exitCode = 1;
            return;
        }

        String commandName = nonOptionArgs.get(0);
        CliCommand command = commandsByName.get(commandName);
        if (command == null) {
            UsagePrinter.printUnknownCommand(commandName, commandsByName.keySet().stream().sorted().toList());
            exitCode = 1;
            return;
        }

        try {
            exitCode = command.run(args);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            exitCode = 1;
        }
    }

    public int exitCode() {
        return exitCode;
    }
}
