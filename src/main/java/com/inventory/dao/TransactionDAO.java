package com.inventory.dao;

import com.inventory.database.DBConnection;
import com.inventory.model.TransactionHistory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    private String safe(String value) {
        return value == null ? "" : value;
    }

    // 🔹 Insert Transaction
    public int createTransaction(
            String buySell,
            String plant,
            String department,
            String location,
            String employeeId,
            String employeeName,
            String ipAddress,
            String itemCode,
            String itemName,
            String itemMake,
            String itemModel,
            String itemSerial,
            String imeiNo,
            String simNo,
            String poNo,
            String partyName,
            String status,
            String remarks,
            String itemCount,
            String unit
    ) {

        String sql = """
        INSERT INTO transactions (
            buy_sell,
            plant,
            department,
            location,
            employee_id,
            employee_name,
            ip_address,
            item_code,
            item_name,
            item_make,
            item_model,
            item_serial,
            imei_no,
            sim_no,
            po_no,
            party_name,
            status,
            issued_datetime,
            remarks,
            item_count,
            unit
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            pstmt.setString(1, buySell);
            pstmt.setString(2, plant);
            pstmt.setString(3, department);
            pstmt.setString(4, location);

            pstmt.setString(5, employeeId);
            pstmt.setString(6, employeeName);

            pstmt.setString(7, ipAddress);

            pstmt.setString(8, itemCode);
            pstmt.setString(9, itemName);
            pstmt.setString(10, itemMake);
            pstmt.setString(11, itemModel);
            pstmt.setString(12, itemSerial);

            pstmt.setString(13, imeiNo);
            pstmt.setString(14, simNo);

            pstmt.setString(15, poNo);
            pstmt.setString(16, partyName);

            pstmt.setString(17, status);

            pstmt.setTimestamp(18, Timestamp.valueOf(LocalDateTime.now()));

            pstmt.setString(19, remarks);

            if (itemCount == null || itemCount.isBlank()) {
                pstmt.setNull(20, Types.DOUBLE);
            } else {
                pstmt.setDouble(20, Double.parseDouble(itemCount));
            }

            pstmt.setString(21, unit);

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();

            if (rs.next()) {
                System.out.println("Transaction ID = " + rs.getInt(1));
                return rs.getInt(1);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // 🔹 Get All Transactions
    public List<TransactionHistory> getAllTransactions() {

        List<TransactionHistory> historyList = new ArrayList<>();

        String sql = """
                SELECT *
                FROM transactions
                ORDER BY issued_datetime DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             );
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {

                Timestamp issuedTs = rs.getTimestamp("issued_datetime");
                Timestamp returnedTs = rs.getTimestamp("returned_datetime");

                String issued = issuedTs != null ? issuedTs.toString() : null;
                String returned = returnedTs != null ? returnedTs.toString() : null;

                double itemCount = rs.getDouble("item_count");
                if (rs.wasNull()) itemCount = 0;

                TransactionHistory history = new TransactionHistory(

                        rs.getInt("transaction_id"),

                        safe(rs.getString("buy_sell")),
                        safe(rs.getString("plant")),
                        safe(rs.getString("department")),
                        safe(rs.getString("location")),

                        safe(rs.getString("employee_id")),
                        safe(rs.getString("employee_name")),

                        safe(rs.getString("ip_address")),

                        safe(rs.getString("item_code")),
                        safe(rs.getString("item_name")),
                        safe(rs.getString("item_make")),
                        safe(rs.getString("item_model")),
                        safe(rs.getString("item_serial")),

                        safe(rs.getString("imei_no")),
                        safe(rs.getString("sim_no")),

                        safe(rs.getString("po_no")),
                        safe(rs.getString("party_name")),

                        safe(rs.getString("status")),

                        issued,
                        returned,

                        safe(rs.getString("remarks")),

                        itemCount,
                        safe(rs.getString("unit")),
                        safe(rs.getString("attachment_file"))
                );

                historyList.add(history);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historyList;
    }

    // 🔹 Get Transactions by Item Code
    public List<TransactionHistory> getTransactionsByItemCode(String itemCode) {

        List<TransactionHistory> historyList = new ArrayList<>();

        String sql = """
                SELECT *
                FROM transactions
                WHERE item_code = ?
                ORDER BY issued_datetime DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, itemCode);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                Timestamp issuedTs = rs.getTimestamp("issued_datetime");
                Timestamp returnedTs = rs.getTimestamp("returned_datetime");

                String issued = issuedTs != null ? issuedTs.toString() : null;
                String returned = returnedTs != null ? returnedTs.toString() : null;

                double itemCount = rs.getDouble("item_count");
                if (rs.wasNull()) itemCount = 0;

                TransactionHistory history = new TransactionHistory(

                        rs.getInt("transaction_id"),

                        safe(rs.getString("buy_sell")),
                        safe(rs.getString("plant")),
                        safe(rs.getString("department")),
                        safe(rs.getString("location")),

                        safe(rs.getString("employee_id")),
                        safe(rs.getString("employee_name")),

                        safe(rs.getString("ip_address")),

                        safe(rs.getString("item_code")),
                        safe(rs.getString("item_name")),
                        safe(rs.getString("item_make")),
                        safe(rs.getString("item_model")),
                        safe(rs.getString("item_serial")),

                        safe(rs.getString("imei_no")),
                        safe(rs.getString("sim_no")),

                        safe(rs.getString("po_no")),
                        safe(rs.getString("party_name")),

                        safe(rs.getString("status")),

                        issued,
                        returned,

                        safe(rs.getString("remarks")),

                        itemCount,
                        safe(rs.getString("unit")),
                        safe(rs.getString("attachment_file"))
                );

                historyList.add(history);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historyList;
    }

    // Get Transactions by field name
    public List<TransactionHistory> getTransactionsByField(String fieldName, String value) {

        List<TransactionHistory> historyList = new ArrayList<>();

        String sql = """
            SELECT *
            FROM transactions
            WHERE %s = ?
            ORDER BY issued_datetime DESC
            """.formatted(fieldName);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, value);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                Timestamp issuedTs = rs.getTimestamp("issued_datetime");
                Timestamp returnedTs = rs.getTimestamp("returned_datetime");

                String issued = issuedTs != null ? issuedTs.toString() : null;
                String returned = returnedTs != null ? returnedTs.toString() : null;

                double itemCount = rs.getDouble("item_count");
                if (rs.wasNull()) itemCount = 0;

                TransactionHistory history = new TransactionHistory(

                        rs.getInt("transaction_id"),

                        safe(rs.getString("buy_sell")),
                        safe(rs.getString("plant")),
                        safe(rs.getString("department")),
                        safe(rs.getString("location")),

                        safe(rs.getString("employee_id")),
                        safe(rs.getString("employee_name")),

                        safe(rs.getString("ip_address")),

                        safe(rs.getString("item_code")),
                        safe(rs.getString("item_name")),
                        safe(rs.getString("item_make")),
                        safe(rs.getString("item_model")),
                        safe(rs.getString("item_serial")),

                        safe(rs.getString("imei_no")),
                        safe(rs.getString("sim_no")),

                        safe(rs.getString("po_no")),
                        safe(rs.getString("party_name")),

                        safe(rs.getString("status")),

                        issued,
                        returned,

                        safe(rs.getString("remarks")),

                        itemCount,
                        safe(rs.getString("unit")),
                        safe(rs.getString("attachment_file"))
                );

                historyList.add(history);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historyList;
    }

    // 🔹 Delete Transaction
    public void deleteTransaction(int transactionId) {

        String sql = "DELETE FROM transactions WHERE transaction_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, transactionId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Update Transaction Status
    public void updateTransactionStatus(int transactionId, String status) {

        String sql = """
        UPDATE transactions
        SET status = ?, returned_datetime = ?
        WHERE transaction_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(3, transactionId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTransactionStatus(int transactionId, String status, String remarks) {
        String sql = "UPDATE transactions SET status = ?, remarks = ? WHERE transaction_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, remarks);
            stmt.setInt(3, transactionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateAttachment(int transactionId, String fileName) {
        String sql =
                "UPDATE transactions SET attachment_file=? WHERE transaction_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fileName);
            ps.setInt(2, transactionId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}