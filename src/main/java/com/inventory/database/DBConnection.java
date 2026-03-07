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
                + "?useSSL=false"
                + "&allowPublicKeyRetrieval=true"
                + "&serverTimezone=UTC"
                + "&connectTimeout=2000"
                + "&socketTimeout=2000";

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {

            // retry once if MySQL restarted
            try {
                Thread.sleep(500);
                return DriverManager.getConnection(url, username, password);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw e;
            }
        }
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

    buy_sell VARCHAR(50),
    plant VARCHAR(100),
    department VARCHAR(100),
    location VARCHAR(100),

    employee_id VARCHAR(100),
    employee_name VARCHAR(255),

    ip_address VARCHAR(100),

    item_code VARCHAR(100),
    item_name VARCHAR(255),
    item_make VARCHAR(255),
    item_model VARCHAR(255),
    item_serial VARCHAR(255),

    item_count DOUBLE,
    unit VARCHAR(50),

    imei_no VARCHAR(100),
    sim_no VARCHAR(100),

    po_no VARCHAR(100),
    party_name VARCHAR(255),

    status VARCHAR(50),

    issued_datetime DATETIME,
    returned_datetime DATETIME,

    remarks TEXT
)
""";

            stmt.execute(createItemsTable);
            stmt.execute(createPersonsTable);
            stmt.execute(createTransactionsTable);
            try {
                stmt.execute("ALTER TABLE transactions ADD COLUMN item_count DOUBLE");
            } catch (SQLException ignored) {}

            try {
                stmt.execute("ALTER TABLE transactions ADD COLUMN unit VARCHAR(50)");
            } catch (SQLException ignored) {}

            System.out.println("MySQL database initialized successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}