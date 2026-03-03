package com.inventory.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

    private static String getDatabasePath() {
        String userHome = System.getProperty("user.home");
        String folderPath = userHome + File.separator + "InventoryManagement";

        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        return folderPath + File.separator + "inventory.db";
    }

    private static final String URL = "jdbc:sqlite:" + getDatabasePath();

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String createItemsTable = """
                CREATE TABLE IF NOT EXISTS items (
                    item_id TEXT PRIMARY KEY,
                    item_name TEXT NOT NULL,
                    description TEXT
                );
                """;

            String createPersonsTable = """
                CREATE TABLE IF NOT EXISTS persons (
                    person_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    employee_id TEXT UNIQUE NOT NULL,
                    person_name TEXT NOT NULL,
                    department TEXT
                );
                """;

            String createTransactionsTable = """
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_id TEXT NOT NULL,
                    person_id INTEGER NOT NULL,
                    issued_datetime TEXT NOT NULL,
                    returned_datetime TEXT,
                    remarks TEXT,
                    FOREIGN KEY (item_id) REFERENCES items(item_id),
                    FOREIGN KEY (person_id) REFERENCES persons(person_id)
                );
                """;

            stmt.execute(createItemsTable);
            stmt.execute(createPersonsTable);
            stmt.execute(createTransactionsTable);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}