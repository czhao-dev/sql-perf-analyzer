package com.sqlanalyzer.report;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ReportWriter {

    public void write(String content, String outputPath) {
        if (outputPath == null || outputPath.isBlank()) {
            System.out.println(content);
            return;
        }
        try {
            Files.writeString(Path.of(outputPath), content);
        } catch (IOException e) {
            throw new UncheckedIOException("failed to write report to " + outputPath, e);
        }
    }
}
