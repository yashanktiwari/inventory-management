package com.inventory.dao;

import com.inventory.database.DBConnection;
import com.inventory.model.Transaction;
import com.inventory.model.TransactionHistory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    // 🔹 Issue Item
    public void issueItem(String itemId, int personId, String remarks) {

        String checkSql = """
                SELECT COUNT(*) FROM transactions
                WHERE item_id = ? AND returned_datetime IS NULL
                """;

        String insertSql = """
                INSERT INTO transactions(item_id, person_id, issued_datetime, remarks)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection()) {

            // Step 1: Check if item already issued
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, itemId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Item is already issued to someone.");
                    return;
                }
            }

            // Step 2: Insert transaction
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, itemId);
                insertStmt.setInt(2, personId);
                insertStmt.setString(3, LocalDateTime.now().toString());
                insertStmt.setString(4, remarks);

                insertStmt.executeUpdate();
                System.out.println("Item issued successfully.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Return Item
    public void returnItem(int itemId) {

        String updateSql = """
                UPDATE transactions
                SET returned_datetime = ?
                WHERE item_id = ? AND returned_datetime IS NULL
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

            pstmt.setString(1, LocalDateTime.now().toString());
            pstmt.setInt(2, itemId);

            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Item returned successfully.");
            } else {
                System.out.println("No active transaction found for this item.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Get Transaction History by Item
    public List<Transaction> getTransactionHistoryByItem(int itemId) {

        List<Transaction> transactions = new ArrayList<>();

        String sql = """
                SELECT * FROM transactions
                WHERE item_id = ?
                ORDER BY issued_datetime DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Transaction transaction = new Transaction(
                        rs.getInt("transaction_id"),
                        rs.getInt("item_id"),
                        rs.getInt("person_id"),
                        rs.getString("issued_datetime"),
                        rs.getString("returned_datetime"),
                        rs.getString("remarks")
                );

                transactions.add(transaction);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    public List<TransactionHistory> getAllTransactions() {

        List<TransactionHistory> historyList = new ArrayList<>();

        String sql = """
                SELECT t.transaction_id,
                                           i.item_id,
                                           i.item_name,
                                           p.employee_id,
                                           p.person_name,
                                           t.issued_datetime,
                                           t.returned_datetime,
                                           t.remarks
            FROM transactions t
            JOIN items i ON t.item_id = i.item_id
            JOIN persons p ON t.person_id = p.person_id
            ORDER BY t.issued_datetime DESC
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                TransactionHistory history = new TransactionHistory(
                        rs.getInt("transaction_id"),
                        rs.getString("item_id"),
                        rs.getString("item_name"),
                        rs.getString("employee_id"),
                        rs.getString("person_name"),
                        rs.getString("issued_datetime"),
                        rs.getString("returned_datetime"),
                        rs.getString("remarks")
                );

                historyList.add(history);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historyList;
    }

    public void returnItemByTransactionId(int transactionId) {

        String sql = """
            UPDATE transactions
            SET returned_datetime = ?
            WHERE transaction_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, java.time.LocalDateTime.now().toString());
            pstmt.setInt(2, transactionId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isItemCurrentlyIssued(String itemId) {

        String sql = """
            SELECT 1 FROM transactions
            WHERE item_id = ?
            AND returned_datetime IS NULL
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, itemId);
            ResultSet rs = pstmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<TransactionHistory> getTransactionsByItemId(String itemId) {

        List<TransactionHistory> historyList = new ArrayList<>();

        String sql = """
        SELECT t.transaction_id,
               i.item_id,
               i.item_name,
               p.employee_id,
               p.person_name,
               t.issued_datetime,
               t.returned_datetime,
               t.remarks
        FROM transactions t
        JOIN items i ON t.item_id = i.item_id
        JOIN persons p ON t.person_id = p.person_id
        WHERE i.item_id = ?
        ORDER BY t.issued_datetime DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                TransactionHistory history = new TransactionHistory(
                        rs.getInt("transaction_id"),
                        rs.getString("item_id"),
                        rs.getString("item_name"),
                        rs.getString("employee_id"),
                        rs.getString("person_name"),
                        rs.getString("issued_datetime"),
                        rs.getString("returned_datetime"),
                        rs.getString("remarks")
                );

                historyList.add(history);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historyList;
    }

    public void deleteTransaction(int transactionId) {

        String sql = "DELETE FROM transactions WHERE transaction_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, transactionId);
            pstmt.executeUpdate();

            System.out.println("Transaction deleted successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}