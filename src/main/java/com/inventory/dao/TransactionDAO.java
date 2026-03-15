package com.inventory.dao;

import com.inventory.database.AppConfig;
import com.inventory.database.DBConnection;
import com.inventory.model.AuditEntry;
import com.inventory.model.InventoryItem;
import com.inventory.model.TransactionHistory;
import com.inventory.util.UserUtil;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TransactionDAO {

    private static final DateTimeFormatter TABLE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
    private static final Set<String> notifiedItems = new HashSet<>();

    private String safe(String value) {
        return value == null ? "" : value;
    }

    // 🔹 Insert Transaction
    public int createTransaction(
            Integer parentTransactionId,
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
            String itemCondition,
            String itemLocation,
            String itemCategory,
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
            parent_transaction_id,
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
            item_condition,
            item_location,
            item_category,
            imei_no,
            sim_no,
            po_no,
            party_name,
            status,
            issued_datetime,
            returned_datetime,
            remarks,
            item_count,
            unit,
            is_available
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            pstmt.setObject(1, parentTransactionId);
            pstmt.setString(2, buySell);
            pstmt.setString(3, plant);
            pstmt.setString(4, department);
            pstmt.setString(5, location);

            pstmt.setString(6, employeeId);
            pstmt.setString(7, employeeName);

            pstmt.setString(8, ipAddress);

            pstmt.setString(9, itemCode);
            pstmt.setString(10, itemName);
            pstmt.setString(11, itemMake);
            pstmt.setString(12, itemModel);
            pstmt.setString(13, itemSerial);
            pstmt.setString(14, itemCondition);
            pstmt.setString(15, itemLocation);
            pstmt.setString(16, itemCategory);

            pstmt.setString(17, imeiNo);
            pstmt.setString(18, simNo);

            pstmt.setString(19, poNo);
            pstmt.setString(20, partyName);

            pstmt.setString(21, status);

            pstmt.setTimestamp(22, Timestamp.valueOf(transactionTime));

            if ("SCRAPPED".equalsIgnoreCase(status)) {
                pstmt.setTimestamp(23, Timestamp.valueOf(LocalDateTime.now()));
            } else {
                pstmt.setNull(23, Types.TIMESTAMP);
            }

            pstmt.setString(24, remarks);

            if (itemCount == null || itemCount.isBlank()) {
                pstmt.setNull(25, Types.DOUBLE);
            } else {
                pstmt.setDouble(25, Double.parseDouble(itemCount));
            }

            pstmt.setString(26, unit);

            boolean isAvailable = "IN STOCK".equalsIgnoreCase(status) || "RETURNED".equalsIgnoreCase(status);
            pstmt.setBoolean(27, isAvailable);

            pstmt.executeUpdate();
            checkLowStock(itemName);

            ResultSet rs = pstmt.getGeneratedKeys();

            if (rs.next()) {
                int transactionId = rs.getInt(1);

                // 🔹 Determine user safely
                String currentUser = UserUtil.getCurrentUser();

                // 🔹 Insert first audit record
                insertAudit(
                        transactionId,
                        currentUser.toUpperCase(),
                        "CREATE_TRANSACTION",
                        "Empty",
                        "Transaction created"
                );

                return transactionId;
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void updateTransaction(
            int transactionId,
            String buySell,
            String plant,
            String department,
            String location,
            String employeeCode,
            String employeeName,
            String ip,
            String itemCode,
            String itemName,
            String itemMake,
            String itemModel,
            String itemSerial,
            String itemCondition,
            String itemLocation,
            String itemCategory,
            String imei,
            String sim,
            String po,
            String party,
            String status,
            String remarks,
            String itemCount,
            String unit,
            String attachmentFile
    ) {

        String updateSql = """
        UPDATE transactions SET
            buy_sell = ?,
            plant = ?,
            department = ?,
            location = ?,
            employee_id = ?,
            employee_name = ?,
            ip_address = ?,
            item_code = ?,
            item_name = ?,
            item_make = ?,
            item_model = ?,
            item_serial = ?,
            item_condition = ?,
            item_location = ?,
            item_category = ?,
            imei_no = ?,
            sim_no = ?,
            po_no = ?,
            party_name = ?,
            status = ?,
            remarks = ?,
            item_count = ?,
            unit = ?,
            attachment_file = ?
        WHERE transaction_id = ?
    """;

        try (Connection conn = DBConnection.getConnection()) {

            // 🔹 Step 1: Fetch existing values
            Map<String, String> oldValues = new HashMap<>();

            String fetchSql = "SELECT * FROM transactions WHERE transaction_id = ?";

            try (PreparedStatement fetchStmt = conn.prepareStatement(fetchSql)) {

                fetchStmt.setInt(1, transactionId);

                ResultSet rs = fetchStmt.executeQuery();

                if (rs.next()) {

                    oldValues.put("buy_sell", rs.getString("buy_sell"));
                    oldValues.put("plant", rs.getString("plant"));
                    oldValues.put("department", rs.getString("department"));
                    oldValues.put("location", rs.getString("location"));
                    oldValues.put("employee_id", rs.getString("employee_id"));
                    oldValues.put("employee_name", rs.getString("employee_name"));
                    oldValues.put("ip_address", rs.getString("ip_address"));
                    oldValues.put("item_code", rs.getString("item_code"));
                    oldValues.put("item_name", rs.getString("item_name"));
                    oldValues.put("item_make", rs.getString("item_make"));
                    oldValues.put("item_model", rs.getString("item_model"));
                    oldValues.put("item_serial", rs.getString("item_serial"));
                    oldValues.put("item_condition", rs.getString("item_condition"));
                    oldValues.put("item_category", rs.getString("item_category"));
                    oldValues.put("item_location", rs.getString("item_location"));
                    oldValues.put("imei_no", rs.getString("imei_no"));
                    oldValues.put("sim_no", rs.getString("sim_no"));
                    oldValues.put("po_no", rs.getString("po_no"));
                    oldValues.put("party_name", rs.getString("party_name"));
                    oldValues.put("status", rs.getString("status"));
                    oldValues.put("remarks", rs.getString("remarks"));
                    oldValues.put("item_count", rs.getString("item_count"));
                    oldValues.put("unit", rs.getString("unit"));
                    oldValues.put("attachment_file", rs.getString("attachment_file"));
                }
            }

            // 🔹 Step 2: Update transaction
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {

                ps.setString(1, buySell);
                ps.setString(2, plant);
                ps.setString(3, department);
                ps.setString(4, location);
                ps.setString(5, employeeCode);
                ps.setString(6, employeeName);
                ps.setString(7, ip);
                ps.setString(8, itemCode);
                ps.setString(9, itemName);
                ps.setString(10, itemMake);
                ps.setString(11, itemModel);
                ps.setString(12, itemSerial);
                ps.setString(13, itemCondition);
                ps.setString(14, itemLocation);
                ps.setString(15, itemCategory);
                ps.setString(16, imei);
                ps.setString(17, sim);
                ps.setString(18, po);
                ps.setString(19, party);
                ps.setString(20, status);
                ps.setString(21, remarks);

                if (itemCount == null || itemCount.isBlank()) {
                    ps.setNull(22, Types.DOUBLE);
                } else {
                    ps.setDouble(22, Double.parseDouble(itemCount));
                }

                ps.setString(23, unit);
                ps.setString(24, attachmentFile);
                ps.setInt(25, transactionId);

                ps.executeUpdate();
                checkLowStock(itemName);

                String oldAttachment = oldValues.get("attachment_file");

                if (oldAttachment != null && !oldAttachment.isBlank() &&
                        !Objects.equals(oldAttachment, attachmentFile)) {

                    File file = new File(
                            AppConfig.getAttachmentPath()
                                    + File.separator +
                                    "transactions" +
                                    File.separator +
                                    oldAttachment
                    );

                    if (file.exists()) {
                        file.delete();
                    }
                }
            }

            // 🔹 Step 3: Insert audit records
            String currentUser = UserUtil.getCurrentUser();

            checkAndAudit(transactionId, currentUser, "buy_sell", oldValues.get("buy_sell"), buySell);
            checkAndAudit(transactionId, currentUser, "plant", oldValues.get("plant"), plant);
            checkAndAudit(transactionId, currentUser, "department", oldValues.get("department"), department);
            checkAndAudit(transactionId, currentUser, "location", oldValues.get("location"), location);
            checkAndAudit(transactionId, currentUser, "employee_id", oldValues.get("employee_id"), employeeCode);
            checkAndAudit(transactionId, currentUser, "employee_name", oldValues.get("employee_name"), employeeName);
            checkAndAudit(transactionId, currentUser, "ip_address", oldValues.get("ip_address"), ip);
            checkAndAudit(transactionId, currentUser, "item_serial", oldValues.get("item_serial"), itemSerial);
            checkAndAudit(transactionId, currentUser, "item_code", oldValues.get("item_code"), itemCode);
            checkAndAudit(transactionId, currentUser, "item_name", oldValues.get("item_name"), itemName);
            checkAndAudit(transactionId, currentUser, "item_make", oldValues.get("item_make"), itemMake);
            checkAndAudit(transactionId, currentUser, "item_model", oldValues.get("item_model"), itemModel);
            checkAndAudit(transactionId, currentUser, "item_condition", oldValues.get("item_condition"), itemCondition);
            checkAndAudit(transactionId, currentUser, "item_location", oldValues.get("item_location"), itemLocation);
            checkAndAudit(transactionId, currentUser, "item_category", oldValues.get("item_category"), itemCategory);
            checkAndAudit(transactionId, currentUser, "imei_no", oldValues.get("imei_no"), imei);
            checkAndAudit(transactionId, currentUser, "sim_no", oldValues.get("sim_no"), sim);
            checkAndAudit(transactionId, currentUser, "po_no", oldValues.get("po_no"), po);
            checkAndAudit(transactionId, currentUser, "party_name", oldValues.get("party_name"), party);
            checkAndAudit(transactionId, currentUser, "status", oldValues.get("status"), status);
            checkAndAudit(transactionId, currentUser, "remarks", oldValues.get("remarks"), remarks);
            checkAndAudit(transactionId, currentUser, "item_count", oldValues.get("item_count"), itemCount);
            checkAndAudit(transactionId, currentUser, "unit", oldValues.get("unit"), unit);
            checkAndAudit(transactionId, currentUser, "Attachment File", oldValues.get("attachment_file"), attachmentFile);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update transaction.");
        }
    }

    // 🔹 Get All Transactions
    public List<TransactionHistory> getAllTransactions() {
        System.out.println("DAO: Fetching all transactions...");

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
                System.out.println(
                        "DAO Row -> ID: " + rs.getInt("transaction_id") +
                                " | Item: " + rs.getString("item_name") +
                                " | Status: " + rs.getString("status") +
                                " | Available: " + rs.getBoolean("is_available")
                );

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
                        safe(rs.getString("item_condition")),
                        safe(rs.getString("item_location")),
                        safe(rs.getString("item_category")),

                        safe(rs.getString("imei_no")),
                        safe(rs.getString("sim_no")),

                        safe(rs.getString("po_no")),
                        safe(rs.getString("party_name")),

                        normalizeStatus(rs.getString("status")),

                        issued,
                        returned,

                        safe(rs.getString("remarks")),

                        itemCount,
                        safe(rs.getString("unit")),
                        safe(rs.getString("attachment_file")),

                        safe(lastModifiedBy),
                        auditEntries,
                        rs.getInt("parent_transaction_id")
                );

                // ✅ read directly from DB
                history.setAvailable(rs.getBoolean("is_available"));

                historyList.add(history);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("DAO: Total transactions fetched = " + historyList.size());
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
                        safe(rs.getString("item_condition")),
                        safe(rs.getString("item_location")),
                        safe(rs.getString("item_category")),

                        safe(rs.getString("imei_no")),
                        safe(rs.getString("sim_no")),

                        safe(rs.getString("po_no")),
                        safe(rs.getString("party_name")),

                        normalizeStatus(rs.getString("status")),

                        issued,
                        returned,

                        safe(rs.getString("remarks")),

                        itemCount,
                        safe(rs.getString("unit")),
                        safe(rs.getString("attachment_file")),

                        safe(lastModifiedBy),
                        auditEntries,
                        rs.getInt("parent_transaction_id")
                );
                history.setAvailable(rs.getBoolean("is_available"));

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
    public void updateTransactionStatus(int transactionId, String status, String remarks) {

        try (Connection conn = DBConnection.getConnection()) {

            // 1️⃣ Fetch OLD transaction
            TransactionHistory oldT = getTransactionById(transactionId);

            String sql = """
        UPDATE transactions
        SET status = ?, 
            is_available = ?, 
            remarks = ?, 
            returned_datetime = ?
        WHERE transaction_id = ?
        """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                boolean available = "RETURNED".equalsIgnoreCase(status);

                stmt.setString(1, status.toUpperCase());
                stmt.setBoolean(2, available);
                stmt.setString(3, remarks);

                if ("RETURNED".equalsIgnoreCase(status) || "SCRAPPED".equalsIgnoreCase(status)) {
                    stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                } else {
                    stmt.setNull(4, Types.TIMESTAMP);
                }

                stmt.setInt(5, transactionId);

                stmt.executeUpdate();
            }

            // 2️⃣ Fetch NEW transaction
            TransactionHistory newT = getTransactionById(transactionId);
            checkLowStock(newT.getItemName());

            // 3️⃣ Log audit differences
            logChanges(oldT, newT, UserUtil.getCurrentUser());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateAttachment(int transactionId, String fileName) {

        String fetchSql =
                "SELECT attachment_file FROM transactions WHERE transaction_id = ?";

        String updateSql =
                "UPDATE transactions SET attachment_file=? WHERE transaction_id=?";

        try (Connection conn = DBConnection.getConnection()) {

            String oldAttachment = null;

            // 🔹 Step 1: Fetch old attachment
            try (PreparedStatement ps = conn.prepareStatement(fetchSql)) {

                ps.setInt(1, transactionId);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    oldAttachment = rs.getString("attachment_file");
                }
            }

            // 🔹 Step 2: Update DB
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {

                if (fileName == null || fileName.isBlank()) {
                    ps.setNull(1, Types.VARCHAR);
                } else {
                    ps.setString(1, fileName);
                }

                ps.setInt(2, transactionId);
                ps.executeUpdate();
            }


            // 🔹 Step 3: Audit
            checkAndAudit(transactionId, UserUtil.getCurrentUser(), "Attachment File", oldAttachment, fileName);

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
            ps.setString(2, user.toUpperCase());
            ps.setString(3, field.toUpperCase());
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
                        safe(rs.getString("item_condition")),
                        safe(rs.getString("item_location")),
                        safe(rs.getString("item_category")),
                        safe(rs.getString("imei_no")),
                        safe(rs.getString("sim_no")),
                        safe(rs.getString("po_no")),
                        safe(rs.getString("party_name")),
                        normalizeStatus(rs.getString("status")),
                        issued,
                        returned,
                        safe(rs.getString("remarks")),
                        rs.getDouble("item_count"),
                        safe(rs.getString("unit")),
                        safe(rs.getString("attachment_file")),
                        "",
                        new ArrayList<>(),
                        rs.getInt("parent_transaction_id")
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
                    "STATUS",
                    oldT.getStatus(),
                    newT.getStatus());
        }

        if (!Objects.equals(oldT.getRemarks(), newT.getRemarks())) {
            insertAudit(oldT.getTransactionId(),
                    user,
                    "REMARKS",
                    oldT.getRemarks(),
                    newT.getRemarks());
        }

        if (!Objects.equals(oldT.getAttachmentFile(), newT.getAttachmentFile())) {
            insertAudit(oldT.getTransactionId(),
                    user,
                    "ATTACHMENT",
                    oldT.getAttachmentFile(),
                    newT.getAttachmentFile());
        }

        if (!Objects.equals(oldT.getItemCount(), newT.getItemCount())) {
            insertAudit(oldT.getTransactionId(),
                    user,
                    "ITEM COUNT",
                    String.valueOf(oldT.getItemCount()),
                    String.valueOf(newT.getItemCount()));
        }
    }

    public List<InventoryItem> getInventory() {

        List<InventoryItem> inventory = new ArrayList<>();

//        String sql = """
//            SELECT
//                item_code,
//                item_name,
//                unit,
//                COALESCE(MAX(minimum_stock), -1) AS minimum_stock,
//                SUM(item_count) AS stock
//            FROM (
//                SELECT *
//                FROM (
//                    SELECT *,
//                           ROW_NUMBER() OVER (
//                               PARTITION BY item_serial
//                               ORDER BY issued_datetime DESC
//                           ) rn
//                    FROM transactions
//                ) latest
//                WHERE rn = 1
//                AND UPPER(status) IN ('IN STOCK','RETURNED')
//            ) current_stock
//            GROUP BY item_code, item_name, unit
//            ORDER BY item_name;
//        """;

        String sql = """
                SELECT
                    item_code,
                    item_name,
                    unit,
                    COALESCE(MAX(minimum_stock), -1) AS minimum_stock,
                    SUM(
                        CASE
                            WHEN UPPER(buy_sell) = 'BUY' THEN item_count
                            WHEN UPPER(status) = 'ISSUED' THEN -item_count
                            WHEN UPPER(status) = 'SCRAPPED' THEN -item_count
                            ELSE 0
                        END
                    ) AS stock
                FROM transactions
                GROUP BY item_code, item_name, unit
                HAVING stock > 0
                ORDER BY item_name;
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                inventory.add(new InventoryItem(
                        rs.getString("item_name"),
                        rs.getString("item_code"),
                        rs.getDouble("stock"),
                        rs.getString("unit"),
                        rs.getDouble("minimum_stock")
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
                WHEN UPPER(buy_sell) = 'BUY' THEN item_count
                WHEN UPPER(status) = 'ISSUED' THEN -item_count
                WHEN UPPER(status) = 'SCRAPPED' THEN -item_count
                ELSE 0
            END
        ) AS stock
        FROM transactions
        WHERE item_name = ?
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


    public double getMinimumStock(String itemName) {

        String sql = """
        SELECT COALESCE(MAX(minimum_stock), -1) AS minimum_stock
        FROM transactions
        WHERE item_name = ?
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemName);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("minimum_stock");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public void checkLowStock(String itemName) {

        double minimum = getMinimumStock(itemName);

        if (minimum < 0) return;

        double currentStock = getCurrentStock(itemName);

        if (currentStock < minimum) {

            com.inventory.util.NotificationUtil.showLowStockNotification(
                    itemName,
                    currentStock,
                    minimum
            );
        }
    }


//    public List<TransactionHistory> getAvailableSerialItems(String itemName) {
//
//        List<TransactionHistory> list = new ArrayList<>();
//
//        String sql = """
//        SELECT *
//        FROM (
//            SELECT *,
//                   ROW_NUMBER() OVER (
//                       PARTITION BY item_serial
//                       ORDER BY issued_datetime DESC
//                   ) rn
//            FROM transactions
//            WHERE item_name = ?
//        ) t
//        WHERE rn = 1
//        AND (status IS NULL OR status NOT IN ('ISSUED','SCRAPPED'))
//        ORDER BY issued_datetime;
//    """;
//
//        try (Connection conn = DBConnection.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//
//            ps.setString(1, itemName);
//            ps.setString(2, itemName);
//
//            ResultSet rs = ps.executeQuery();
//
//            while (rs.next()) {
//
//                Timestamp issuedTs = rs.getTimestamp("issued_datetime");
//
//                LocalDateTime issued =
//                        issuedTs != null ? issuedTs.toLocalDateTime() : null;
//
//                String serial = rs.getString("item_serial");
//                if (serial == null) serial = "";
//
//                TransactionHistory history = new TransactionHistory(
//
//                        rs.getInt("transaction_id"),
//
//                        rs.getString("buy_sell"),
//                        "",
//                        "",
//                        "",
//
//                        "",
//                        "",
//
//                        "",
//
//                        rs.getString("item_code"),
//                        itemName,
//
//                        rs.getString("item_make"),
//                        rs.getString("item_model"),
//                        serial,
//                        rs.getString("item_condition"),
//                        "",
//                        "",
//
//                        "",
//                        "",
//
//                        "",
//                        "",
//
//                        rs.getString("status"),
//
//                        issued,
//                        null,
//
//                        "",
//
//                        rs.getDouble("item_count"),
//                        rs.getString("unit"),
//
//                        "",
//
//                        "",
//                        new ArrayList<>(),
//                        rs.getInt("parent_transaction_id")
//                );
//
//                list.add(history);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return list;
//    }

    public List<TransactionHistory> getAvailableSerialItems(String itemName) {

        List<TransactionHistory> list = new ArrayList<>();

        String sql = """
        SELECT *
        FROM (
            SELECT *,
                   ROW_NUMBER() OVER (
                       PARTITION BY item_serial
                       ORDER BY issued_datetime DESC
                   ) rn
            FROM transactions
            WHERE item_name = ?
        ) t
        WHERE rn = 1
        AND (status IS NULL OR status NOT IN ('ISSUED','SCRAPPED'))
        ORDER BY issued_datetime
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Only ONE parameter exists in the query
            ps.setString(1, itemName);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Timestamp issuedTs = rs.getTimestamp("issued_datetime");

                LocalDateTime issued =
                        issuedTs != null ? issuedTs.toLocalDateTime() : null;

                String serial = rs.getString("item_serial");
                if (serial == null) serial = "";

                TransactionHistory history = new TransactionHistory(

                        rs.getInt("transaction_id"),

                        rs.getString("buy_sell"),
                        "",
                        "",
                        "",

                        "",
                        "",

                        "",

                        rs.getString("item_code"),
                        rs.getString("item_name"),

                        rs.getString("item_make"),
                        rs.getString("item_model"),
                        serial,
                        rs.getString("item_condition"),
                        "",
                        "",

                        "",
                        "",

                        "",
                        "",

                        rs.getString("status"),

                        issued,
                        null,

                        "",

                        rs.getDouble("item_count"),
                        rs.getString("unit"),

                        "",

                        "",
                        new ArrayList<>(),
                        rs.getInt("parent_transaction_id")
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
        WHERE UPPER(buy_sell) = 'BUY'
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

    public boolean isDemoLimitReached() {
        String sql = "SELECT COUNT(*) FROM transactions";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1) >= 100;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean serialExists(String itemSerial) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE item_serial = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemSerial);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void checkAndAudit(
            int transactionId,
            String user,
            String field,
            String oldValue,
            String newValue
    ) {

        String oldVal = oldValue == null ? "" : oldValue.trim();
        String newVal = newValue == null ? "" : newValue.trim();

        // 🔹 Special handling for numeric fields
        if ("item_count".equals(field)) {

            try {

                double oldNum = oldVal.isBlank() ? 0 : Double.parseDouble(oldVal);
                double newNum = newVal.isBlank() ? 0 : Double.parseDouble(newVal);

                if (Double.compare(oldNum, newNum) != 0) {
                    insertAudit(transactionId, user, field, oldVal, newVal);
                }

            } catch (Exception e) {

                if (!oldVal.equals(newVal)) {
                    insertAudit(transactionId, user, field, oldVal, newVal);
                }
            }

            return;
        }

        // 🔹 Special handling for Attachment
        if ("Attachment File".equals(field)) {

            String oldDisplay = oldVal.isBlank() ? "Empty" : oldVal;
            String newDisplay;

            if (newVal.isBlank()) {
                newDisplay = oldVal.isBlank() ? "Empty" : "Deleted";
            } else {
                newDisplay = newVal;
            }

            if (!oldDisplay.equals(newDisplay)) {
                insertAudit(transactionId, user, field, oldDisplay, newDisplay);
            }

            return;
        }

        // 🔹 Default comparison (ignore case + trim)
        if (!oldVal.equalsIgnoreCase(newVal)) {
            insertAudit(transactionId, user, field, oldVal, newVal);
        }
    }

    public void updateMinimumStock(String itemName, double minimum) {

        String sql = """
        UPDATE transactions
        SET minimum_stock = ?
        WHERE item_name = ?
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, minimum);
            ps.setString(2, itemName);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<TransactionHistory> getBuyTransactions() {

        String sql = """
        SELECT *
        FROM transactions
        WHERE UPPER(buy_sell) = 'Buy'
        ORDER BY issued_datetime DESC
    """;

        return fetchTransactions(sql);
    }

    public List<TransactionHistory> getInStockTransactions() {

        String sql = """
        SELECT *
        FROM (
            SELECT *,
                   ROW_NUMBER() OVER (
                       PARTITION BY item_serial
                       ORDER BY issued_datetime DESC
                   ) rn
            FROM transactions
        ) t
        WHERE rn = 1
        AND UPPER(status) IN ('IN STOCK','RETURNED')
        ORDER BY issued_datetime DESC;
    """;

        return fetchTransactions(sql);
    }

    public List<TransactionHistory> getIssuedTransactions() {
        String sql = """
                SELECT *
                FROM (
                    SELECT *,
                           ROW_NUMBER() OVER (
                               PARTITION BY item_serial
                               ORDER BY issued_datetime DESC
                           ) rn
                    FROM transactions
                ) t
                WHERE rn = 1
                AND status = 'ISSUED';
                """;

        List<TransactionHistory> issuedTransactions = fetchTransactions(sql);
        return issuedTransactions;
    }

    public List<TransactionHistory> getScrappedTransactions() {

        String sql = """
        SELECT *
        FROM (
            SELECT *,
                   ROW_NUMBER() OVER (
                       PARTITION BY item_serial
                       ORDER BY issued_datetime DESC
                   ) rn
            FROM transactions
        ) t
        WHERE rn = 1
        AND UPPER(status) = 'SCRAPPED'
        ORDER BY returned_datetime DESC;
    """;

        return fetchTransactions(sql);
    }

    public List<TransactionHistory> getReturnedTransactions() {

        String sql = """
        SELECT *
        FROM transactions
        WHERE UPPER(status) = 'RETURNED'
        ORDER BY returned_datetime DESC
    """;

        return fetchTransactions(sql);
    }

    private List<TransactionHistory> fetchTransactions(String sql) {

        List<TransactionHistory> historyList = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {

                Timestamp issuedTs = rs.getTimestamp("issued_datetime");
                Timestamp returnedTs = rs.getTimestamp("returned_datetime");

                LocalDateTime issued =
                        issuedTs != null ? issuedTs.toLocalDateTime() : null;

                LocalDateTime returned =
                        returnedTs != null ? returnedTs.toLocalDateTime() : null;

                List<AuditEntry> auditEntries =
                        getAuditTrail(rs.getInt("transaction_id"));

                String lastModifiedBy =
                        auditEntries.isEmpty() ? "" : auditEntries.get(0).getModifiedBy();

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
                        safe(rs.getString("item_condition")),
                        safe(rs.getString("item_location")),
                        safe(rs.getString("item_category")),

                        safe(rs.getString("imei_no")),
                        safe(rs.getString("sim_no")),

                        safe(rs.getString("po_no")),
                        safe(rs.getString("party_name")),

                        normalizeStatus(rs.getString("status")),

                        issued,
                        returned,

                        safe(rs.getString("remarks")),

                        itemCount,
                        safe(rs.getString("unit")),
                        safe(rs.getString("attachment_file")),

                        safe(lastModifiedBy),
                        auditEntries,
                        rs.getInt("parent_transaction_id")
                );

                // ✅ Use DB column instead of DB call
                history.setAvailable(rs.getBoolean("is_available"));

                historyList.add(history);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historyList;
    }

    private String normalizeStatus(String status) {
        if (status == null) return "";
        status = status.trim().toUpperCase();
        return switch (status) {
            case "IN STOCK", "IN_STOCK" -> "IN STOCK";
            case "ISSUED" -> "ISSUED";
            case "RETURNED" -> "RETURNED";
            case "SCRAP", "SCRAPPED" -> "SCRAPPED";
            default -> status;
        };
    }

    public boolean isItemAvailableBySerial(String serial) {

        String sql = """
        SELECT is_available
        FROM transactions
        WHERE item_serial = ?
        ORDER BY issued_datetime DESC
        LIMIT 1
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, serial);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("is_available");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public int insertTransactionFromExcel(TransactionHistory t) {

        String sql = """
        INSERT INTO transactions (
            parent_transaction_id,
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
            item_condition,
            item_location,
            item_category,
            imei_no,
            sim_no,
            po_no,
            party_name,
            status,
            issued_datetime,
            returned_datetime,
            remarks,
            item_count,
            unit,
            is_available
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject(1, t.getParentTransactionId());
            ps.setString(2, t.getBuySell());
            ps.setString(3, t.getPlant());
            ps.setString(4, t.getDepartment());
            ps.setString(5, t.getLocation());

            ps.setString(6, t.getEmployeeCode());
            ps.setString(7, t.getEmployeeName());
            ps.setString(8, "");

            ps.setString(9, t.getItemCode());
            ps.setString(10, t.getItemName());
            ps.setString(11, t.getItemMake());
            ps.setString(12, t.getItemModel());
            ps.setString(13, t.getItemSerial());
            ps.setString(14, t.getItemCondition());
            ps.setString(15, t.getItemLocation());
            ps.setString(16, t.getItemCategory());

            ps.setString(17, t.getImeiNo());
            ps.setString(18, t.getSimNo());

            ps.setString(19, t.getPoNo());
            ps.setString(20, t.getPartyName());

            ps.setString(21, t.getStatus());

            ps.setObject(22, t.getIssuedDateTime());
            ps.setObject(23, t.getReturnedDateTime());

            ps.setString(24, t.getRemarks());

            ps.setDouble(25, t.getItemCount());
            ps.setString(26, t.getUnit());

            ps.setBoolean(27, t.isAvailable());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public Integer findAvailableParent(String itemCode, String serial) {

        String sql = """
        SELECT transaction_id
        FROM transactions
        WHERE item_code = ?
        AND item_serial = ?
        AND is_available = true
        ORDER BY issued_datetime DESC
        LIMIT 1
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemCode);
            ps.setString(2, serial);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("transaction_id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void insertAuditWithTime(
            int transactionId,
            String user,
            LocalDateTime modifiedAt,
            String field,
            String oldVal,
            String newVal
    ) {

        String sql = """
        INSERT INTO transaction_audit
        (transaction_id, modified_by, modified_at, field_name, old_value, new_value)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transactionId);
            ps.setString(2, user);
            ps.setTimestamp(3, Timestamp.valueOf(modifiedAt));
            ps.setString(4, field);
            ps.setString(5, oldVal);
            ps.setString(6, newVal);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}