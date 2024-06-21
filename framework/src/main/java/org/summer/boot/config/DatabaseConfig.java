package org.summer.boot.config;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

public class DatabaseConfig {
    private static String databaseUrl;

    public DatabaseConfig(String databaseUrl) {
        databaseUrl = databaseUrl;
    }

    public static ConnectionSource getConnectionSource() throws SQLException {
        return new JdbcConnectionSource(databaseUrl);
    }
}
