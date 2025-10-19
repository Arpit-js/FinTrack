package com.fynance;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final String CONFIG_FILE = "config.properties";
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        Properties props = new Properties();
        // This is the corrected way to load a resource file in a Maven project
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Cannot find " + CONFIG_FILE + " in the classpath.");
            }
            props.load(input);
            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");
            if (URL == null || USER == null || PASSWORD == null) {
                throw new RuntimeException("Database configuration incomplete in " + CONFIG_FILE);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load " + CONFIG_FILE + ": " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
