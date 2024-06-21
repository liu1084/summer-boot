package org.summer.boot.config;


import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import lombok.Getter;

import java.io.IOException;
import java.sql.SQLException;

public class DatabaseAutoConfiguration {
    @Getter
    private static ConnectionSource connectionSource;

    public static void initialize(String databaseUrl) throws SQLException {
        connectionSource = new JdbcConnectionSource(databaseUrl);
    }

    public static void closeConnection() throws IOException {
        if (connectionSource != null) {
            connectionSource.close();
        }
    }
}

