package com.sqlanalyzer.db;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Translates the Go-style {@code postgres://user:pass@host:port/db?sslmode=disable}
 * connection URL (used throughout the original README) into a JDBC URL plus
 * credentials, since the PostgreSQL JDBC driver does not accept that scheme directly.
 */
public final class DatabaseUrlParser {

    private static final int DEFAULT_PORT = 5432;

    private DatabaseUrlParser() {
    }

    public record JdbcConnectionInfo(String jdbcUrl, String username, String password) {
    }

    public static JdbcConnectionInfo parse(String databaseUrl) {
        if (databaseUrl == null || databaseUrl.isBlank()) {
            throw new IllegalArgumentException("database url must not be blank");
        }
        if (databaseUrl.startsWith("jdbc:")) {
            return new JdbcConnectionInfo(databaseUrl, null, null);
        }

        URI uri = URI.create(databaseUrl);
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equals("postgres") || scheme.equals("postgresql"))) {
            throw new IllegalArgumentException("unsupported database url scheme: " + scheme);
        }

        String username = null;
        String password = null;
        String userInfo = uri.getRawUserInfo();
        if (userInfo != null) {
            String[] parts = userInfo.split(":", 2);
            username = decode(parts[0]);
            if (parts.length > 1) {
                password = decode(parts[1]);
            }
        }

        String host = uri.getHost() != null ? uri.getHost() : "localhost";
        int port = uri.getPort() != -1 ? uri.getPort() : DEFAULT_PORT;
        String path = uri.getPath() == null ? "" : uri.getPath();
        String query = uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "";

        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path + query;
        return new JdbcConnectionInfo(jdbcUrl, username, password);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
