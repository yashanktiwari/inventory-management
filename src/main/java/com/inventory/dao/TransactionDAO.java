package com.inventory.dao;

import com.inventory.database.DBConnection;
import com.inventory.model.TransactionHistory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    // 🔹 Insert Transaction
    public void addTransaction(
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
            String remarks
    ) {

        String sql = """
                INSERT INTO transactions(
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
                    remarks
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Return Item
    public void returnItemByTransactionId(int transactionId) {

        String sql = """
                UPDATE transactions
                SET returned_datetime = ?, status = 'Returned'
                WHERE transaction_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(2, transactionId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
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
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {

                Timestamp issuedTs = rs.getTimestamp("issued_datetime");
                Timestamp returnedTs = rs.getTimestamp("returned_datetime");

                String issued = issuedTs != null ? issuedTs.toString() : null;
                String returned = returnedTs != null ? returnedTs.toString() : null;

                TransactionHistory history = new TransactionHistory(

                        rs.getInt("transaction_id"),

                        rs.getString("buy_sell"),
                        rs.getString("plant"),
                        rs.getString("department"),
                        rs.getString("location"),

                        rs.getString("employee_id"),
                        rs.getString("employee_name"),

                        rs.getString("ip_address"),

                        rs.getString("item_code"),
                        rs.getString("item_name"),
                        rs.getString("item_make"),
                        rs.getString("item_model"),
                        rs.getString("item_serial"),

                        rs.getString("imei_no"),
                        rs.getString("sim_no"),

                        rs.getString("po_no"),
                        rs.getString("party_name"),

                        rs.getString("status"),

                        issued,
                        returned,

                        rs.getString("remarks")
                );

                historyList.add(history);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historyList;
    }

    // 🔹 Search by Item Code
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

                TransactionHistory history = new TransactionHistory(

                        rs.getInt("transaction_id"),

                        rs.getString("buy_sell"),
                        rs.getString("plant"),
                        rs.getString("department"),
                        rs.getString("location"),

                        rs.getString("employee_id"),
                        rs.getString("employee_name"),

                        rs.getString("ip_address"),

                        rs.getString("item_code"),
                        rs.getString("item_name"),
                        rs.getString("item_make"),
                        rs.getString("item_model"),
                        rs.getString("item_serial"),

                        rs.getString("imei_no"),
                        rs.getString("sim_no"),

                        rs.getString("po_no"),
                        rs.getString("party_name"),

                        rs.getString("status"),

                        issued,
                        returned,

                        rs.getString("remarks")
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

    public void createTransaction(
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
            String remarks
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
            remarks
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
}