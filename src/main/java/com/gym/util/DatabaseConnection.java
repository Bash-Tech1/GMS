package com.gym.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private final String url;
    private final String username;
    private final String password;

    private DatabaseConnection() {
        Properties props = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/database.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                System.err.println("database.properties not found on classpath. Using default settings.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load database.properties", e);
        }

        String host = props.getProperty("db.host", "localhost");
        String port = props.getProperty("db.port", "3306");
        String dbName = props.getProperty("db.name", "gym_management_system");
        this.username = props.getProperty("db.user", "root");
        this.password = props.getProperty("db.password", "");

        this.url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}


