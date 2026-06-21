package com.sqlanalyzer.config;

import com.sqlanalyzer.db.DatabaseUrlParser;
import com.sqlanalyzer.db.DatabaseUrlParser.JdbcConnectionInfo;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.time.Duration;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(AnalyzerProperties properties) {
        JdbcConnectionInfo info = DatabaseUrlParser.parse(properties.database().url());

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(info.jdbcUrl());
        if (info.username() != null) {
            dataSource.setUsername(info.username());
        }
        if (info.password() != null) {
            dataSource.setPassword(info.password());
        }

        Duration statementTimeout = properties.database().statementTimeout();
        if (statementTimeout != null && !statementTimeout.isZero()) {
            dataSource.setConnectionInitSql("SET statement_timeout = " + statementTimeout.toMillis());
        }

        return dataSource;
    }
}
