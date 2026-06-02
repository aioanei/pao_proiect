package org.trading.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {
    private static final String DEFAULT_DRIVER = "org.h2.Driver";
    private static final String DEFAULT_URL = "jdbc:h2:./data/trading_db";
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";

    private static DatabaseConnection instance;

    private final String url;
    private final String user;
    private final String password;

    private DatabaseConnection() {
        Properties properties = loadProperties();
        String driver = properties.getProperty("db.driver", DEFAULT_DRIVER);
        this.url = properties.getProperty("db.url", DEFAULT_URL);
        this.user = properties.getProperty("db.user", DEFAULT_USER);
        this.password = properties.getProperty("db.password", DEFAULT_PASSWORD);

        createDataDirectory();
        loadDriver(driver);
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        Path configPath = Path.of("database.properties");

        if (!Files.exists(configPath)) {
            return properties;
        }

        try (InputStream inputStream = Files.newInputStream(configPath)) {
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Nu s-a putut citi configurarea bazei de date.", e);
        }
    }

    private void createDataDirectory() {
        try {
            Files.createDirectories(Path.of("data"));
        } catch (IOException e) {
            throw new IllegalStateException("Nu s-a putut crea directorul data pentru baza de date.", e);
        }
    }

    private void loadDriver(String driver) {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver JDBC lipsa din classpath: " + driver, e);
        }
    }
}
