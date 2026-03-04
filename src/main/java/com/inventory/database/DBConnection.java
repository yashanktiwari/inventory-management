package com.inventory.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

    private static String host;
    private static String port;
    private static String databaseName;
    private static String username;
    private static String password;

    public static void setDatabaseConfig(
            String host,
            String port,
            String databaseName,
            String username,
            String password
    ) {
        DBConnection.host = host;
        DBConnection.port = port;
        DBConnection.databaseName = databaseName;
        DBConnection.username = username;
        DBConnection.password = password;
    }

    public static String getHost() {
        return host;
    }

    public static String getPort() {
        return port;
    }

    public static String getDatabaseName() {
        return databaseName;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static boolean isDatabaseSet() {
        return host != null;
    }

    public static Connection getConnection() throws SQLException {

        if (!isDatabaseSet()) {
            throw new SQLException("Database not configured.");
        }

        String url = "jdbc:mysql://" + host + ":" + port + "/" + databaseName
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        return DriverManager.getConnection(url, username, password);
    }

    // 🔥 Create DB if not exists
    public static void createDatabaseIfNotExists() throws SQLException {

        String url = "jdbc:mysql://" + host + ":" + port
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);
        }
    }

    public static void initializeDatabase() {

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String createItemsTable = """
                CREATE TABLE IF NOT EXISTS items (
                    item_id VARCHAR(100) PRIMARY KEY,
                    item_name VARCHAR(255) NOT NULL,
                    description TEXT
                )
                """;

            String createPersonsTable = """
                CREATE TABLE IF NOT EXISTS persons (
                    person_id INT AUTO_INCREMENT PRIMARY KEY,
                    employee_id VARCHAR(100) UNIQUE NOT NULL,
                    person_name VARCHAR(255) NOT NULL,
                    department VARCHAR(255)
                )
                """;

            String createTransactionsTable = """
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
                    item_id VARCHAR(100) NOT NULL,
                    person_id INT NOT NULL,
                    issued_datetime DATETIME NOT NULL,
                    returned_datetime DATETIME,
                    remarks TEXT,
                    FOREIGN KEY (item_id) REFERENCES items(item_id),
                    FOREIGN KEY (person_id) REFERENCES persons(person_id)
                )
                """;

            stmt.execute(createItemsTable);
            stmt.execute(createPersonsTable);
            stmt.execute(createTransactionsTable);

            System.out.println("MySQL database initialized successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}