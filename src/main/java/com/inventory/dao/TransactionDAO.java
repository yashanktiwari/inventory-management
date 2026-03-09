package com.inventory.dao;

import com.inventory.database.DBConnection;
import com.inventory.model.AuditEntry;
import com.inventory.model.InventoryItem;
import com.inventory.model.TransactionHistory;
import com.inventory.util.UserUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TransactionDAO {

    private static final DateTimeFormatter TABLE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

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
            String unit,
            LocalDateTime transactionTime
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
            returned_datetime,
            remarks,
            item_count,
            unit
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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

//            pstmt.setTimestamp(18, Timestamp.valueOf(LocalDateTime.now()));

            pstmt.setTimestamp(18, Timestamp.valueOf(transactionTime));

            if ("Scrap".equalsIgnoreCase(status)) {
                pstmt.setTimestamp(19, Timestamp.valueOf(LocalDateTime.now()));
            } else {
                pstmt.setNull(19, Types.TIMESTAMP);
            }

            pstmt.setString(20, remarks);

            if (itemCount == null || itemCount.isBlank()) {
                pstmt.setNull(21, Types.DOUBLE);
            } else {
                pstmt.setDouble(21, Double.parseDouble(itemCount));
            }

            pstmt.setString(22, unit);

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();

            if (rs.next()) {
                int transactionId = rs.getInt(1);

                // 🔹 Determine user safely
                String currentUser = UserUtil.getCurrentUser();

                // 🔹 Insert first audit record
                insertAudit(
                        transactionId,
                        currentUser,
                        "CREATE_TRANSACTION",
                        "",
                        "Transaction created"
                );

                return transactionId;
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

                LocalDateTime issued = issuedTs != null
                        ? issuedTs.toLocalDateTime()
                        : null;
                LocalDateTime returned = returnedTs != null
                        ? returnedTs.toLocalDateTime()
                        : null;

                List<AuditEntry> auditEntries =
                        getAuditTrail(rs.getInt("transaction_id"));

                String lastModifiedBy = "";

                if (!auditEntries.isEmpty()) {
                    lastModifiedBy = auditEntries.get(0).getModifiedBy();
                }

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
                        safe(rs.getString("attachment_file")),

                        safe(lastModifiedBy),
                        auditEntries
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

                LocalDateTime issued = issuedTs != null ? issuedTs.toLocalDateTime() : null;
                LocalDateTime returned = returnedTs != null ? returnedTs.toLocalDateTime() : null;

                List<AuditEntry> auditEntries =
                        getAuditTrail(rs.getInt("transaction_id"));

                String lastModifiedBy = "";

                if (!auditEntries.isEmpty()) {
                    lastModifiedBy = auditEntries.get(0).getModifiedBy();
                }

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
                        safe(rs.getString("attachment_file")),

                        safe(lastModifiedBy),
                        auditEntries
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

        try (Connection conn = DBConnection.getConnection()) {

            TransactionHistory oldT = getTransactionById(transactionId);

            String sql = """
            UPDATE transactions
            SET status = ?, returned_datetime = ?
            WHERE transaction_id = ?
        """;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, status);
                pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setInt(3, transactionId);

                pstmt.executeUpdate();
            }

            TransactionHistory newT = getTransactionById(transactionId);

            logChanges(oldT, newT, UserUtil.getCurrentUser());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTransactionStatus(int transactionId, String status, String remarks) {

        try (Connection conn = DBConnection.getConnection()) {

            // 1️⃣ Fetch OLD transaction
            TransactionHistory oldT = getTransactionById(transactionId);

            String sql = """
            UPDATE transactions
            SET status = ?, remarks = ?, returned_datetime = ?
            WHERE transaction_id = ?
        """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, status);
                stmt.setString(2, remarks);

                if ("Returned".equalsIgnoreCase(status)) {
                    stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                } else {
                    stmt.setNull(3, Types.TIMESTAMP);
                }

                stmt.setInt(4, transactionId);

                stmt.executeUpdate();
            }

            // 2️⃣ Fetch NEW transaction
            TransactionHistory newT = getTransactionById(transactionId);

            // 3️⃣ Log audit differences
            logChanges(oldT, newT, UserUtil.getCurrentUser());

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

    public void insertAudit(int transactionId,
                            String user,
                            String field,
                            String oldVal,
                            String newVal) {

        String sql = """
            INSERT INTO transaction_audit
            (transaction_id, modified_by, modified_at, field_name, old_value, new_value)
            VALUES (?, ?, NOW(), ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transactionId);
            ps.setString(2, user);
            ps.setString(3, field);
            ps.setString(4, oldVal);
            ps.setString(5, newVal);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<AuditEntry> getAuditTrail(int transactionId) {

        List<AuditEntry> list = new ArrayList<>();

        String sql = """
            SELECT modified_by, modified_at,
                   field_name, old_value, new_value
            FROM transaction_audit
            WHERE transaction_id = ?
            ORDER BY modified_at DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transactionId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                list.add(new AuditEntry(
                        rs.getString("modified_by"),
                        rs.getTimestamp("modified_at").toLocalDateTime(),
                        rs.getString("field_name"),
                        rs.getString("old_value"),
                        rs.getString("new_value")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public TransactionHistory getTransactionById(int transactionId) {

        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transactionId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Timestamp issuedTs = rs.getTimestamp("issued_datetime");
                Timestamp returnedTs = rs.getTimestamp("returned_datetime");

                LocalDateTime issued = issuedTs != null ? issuedTs.toLocalDateTime() : null;
                LocalDateTime returned = returnedTs != null ? returnedTs.toLocalDateTime() : null;

                return new TransactionHistory(
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
                        rs.getDouble("item_count"),
                        safe(rs.getString("unit")),
                        safe(rs.getString("attachment_file")),
                        "",
                        new ArrayList<>()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void logChanges(TransactionHistory oldT,
                           TransactionHistory newT,
                           String user) {

        if (!Objects.equals(oldT.getStatus(), newT.getStatus())) {
            insertAudit(oldT.getTransactionId(),
                    user,
                    "Status",
                    oldT.getStatus(),
                    newT.getStatus());
        }

        if (!Objects.equals(oldT.getRemarks(), newT.getRemarks())) {
            insertAudit(oldT.getTransactionId(),
                    user,
                    "Remarks",
                    oldT.getRemarks(),
                    newT.getRemarks());
        }

        if (!Objects.equals(oldT.getAttachmentFile(), newT.getAttachmentFile())) {
            insertAudit(oldT.getTransactionId(),
                    user,
                    "Attachment",
                    oldT.getAttachmentFile(),
                    newT.getAttachmentFile());
        }

        if (!Objects.equals(oldT.getItemCount(), newT.getItemCount())) {
            insertAudit(oldT.getTransactionId(),
                    user,
                    "Item Count",
                    String.valueOf(oldT.getItemCount()),
                    String.valueOf(newT.getItemCount()));
        }
    }

    public List<InventoryItem> getInventory() {

        List<InventoryItem> inventory = new ArrayList<>();

        String sql = """
            SELECT
                item_name,
                unit,
                SUM(
                    CASE
                        WHEN buy_sell = 'Buy' THEN item_count
                        WHEN buy_sell = 'Sell' AND status = 'Returned' THEN 0
                        WHEN buy_sell = 'Sell' THEN -item_count
                        ELSE 0
                    END
                ) AS stock
            FROM transactions
            GROUP BY item_name, unit
            HAVING stock > 0
            ORDER BY item_name;
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                inventory.add(new InventoryItem(
                        rs.getString("item_name"),
                        rs.getDouble("stock"),
                        rs.getString("unit")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return inventory;
    }

    public String getUnitForItem(String itemName) {

        String sql = """
        SELECT unit
        FROM transactions
        WHERE item_name = ?
        LIMIT 1
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemName);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("unit");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public double getCurrentStock(String itemName) {

        String sql = """
            SELECT
            SUM(
                CASE
                    WHEN buy_sell = 'Buy' THEN item_count
                    WHEN buy_sell = 'Sell' AND status = 'Returned' THEN 0
                    WHEN buy_sell = 'Sell' THEN -item_count
                    ELSE 0
                END
            ) AS stock
            FROM transactions
            WHERE item_name = ?;
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemName);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("stock");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public List<TransactionHistory> getAvailableSerialItems(String itemName) {
        List<TransactionHistory> list = new ArrayList<>();
        String sql = """
        SELECT
            item_serial,
            item_make,
            item_model,
            unit,
            MIN(issued_datetime) AS issued_datetime,
            SUM(
                CASE
                    WHEN buy_sell = 'Buy' THEN item_count
                    WHEN buy_sell = 'Sell' AND status = 'Returned' THEN 0
                    WHEN buy_sell = 'Sell' THEN -item_count
                    ELSE 0
                END
            ) AS remaining_count
        FROM transactions
        WHERE item_name = ?
        GROUP BY item_serial, item_make, item_model, unit
        HAVING remaining_count > 0
        ORDER BY issued_datetime;
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemName);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Timestamp issuedTs = rs.getTimestamp("issued_datetime");

                LocalDateTime issued =
                        issuedTs != null ? issuedTs.toLocalDateTime() : null;

                String serial = rs.getString("item_serial");
                if (serial == null || serial.isEmpty()) {
                    serial = "";
                }

                TransactionHistory history = new TransactionHistory(

                        0,                          // transaction_id (not needed here)

                        "Buy",                      // buy_sell
                        "", "", "",                 // plant, department, location
                        "", "",                     // employee_id, employee_name
                        "",                         // ip_address

                        "",                         // item_code
                        itemName,                   // item_name

                        rs.getString("item_make"),
                        rs.getString("item_model"),
                        serial,

                        "", "",                     // imei_no, sim_no
                        "", "",                     // po_no, party_name

                        "",                         // status

                        issued,
                        null,

                        "",                         // remarks

                        rs.getDouble("remaining_count"),

                        rs.getString("unit"),

                        "",                         // attachment
                        "",                         // lastModifiedBy
                        new ArrayList<>()
                );

                list.add(history);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean itemExistsInInventory(
            String itemCode,
            String itemName,
            String itemMake,
            String itemModel,
            String itemSerial
    ) {

        String sql = """
        SELECT COUNT(*) 
        FROM transactions
        WHERE buy_sell = 'Buy'
        AND item_code = ?
        AND item_name = ?
        AND item_make = ?
        AND item_model = ?
        AND item_serial = ?
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemCode);
            ps.setString(2, itemName);
            ps.setString(3, itemMake);
            ps.setString(4, itemModel);
            ps.setString(5, itemSerial);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean isSerialAvailable(
            String itemCode,
            String itemName,
            String itemMake,
            String itemModel,
            String itemSerial
    ) {

        String sql = """
        SELECT
            SUM(
                CASE
                    WHEN buy_sell = 'Buy' THEN 1
                    WHEN buy_sell = 'Sell' AND status = 'Issued' THEN -1
                    WHEN buy_sell = 'Sell' AND status = 'Scrap' THEN -1
                    ELSE 0
                END
            ) AS balance
        FROM transactions
        WHERE item_name = ?
        AND (? IS NULL OR item_code = ?)
        AND (? IS NULL OR item_make = ?)
        AND (? IS NULL OR item_model = ?)
        AND (? IS NULL OR item_serial = ?)
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemCode);
            ps.setString(2, itemName);
            ps.setString(3, itemMake);
            ps.setString(4, itemModel);
            ps.setString(5, itemSerial);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int balance = rs.getInt(1);
                return balance > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}