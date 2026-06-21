package com.sqlanalyzer;

import com.sqlanalyzer.cli.AnalyzerCliRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SqlAnalyzerApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SqlAnalyzerApplication.class, args);
        int exitCode = context.getBean(AnalyzerCliRunner.class).exitCode();
        SpringApplication.exit(context, () -> exitCode);
        System.exit(exitCode);
    }
}
