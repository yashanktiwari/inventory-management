package com.inventory.dao;

import com.inventory.database.DBConnection;
import com.inventory.model.WorkLog;
import com.inventory.model.WorkType;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WorkLogDAO {

    public boolean insertWorkLog(WorkLog workLog) {

        String sql = """
                INSERT INTO work_log (username, work_type, details)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, workLog.getUsername());
            ps.setString(2, workLog.getWorkType().name());
            ps.setString(3, workLog.getDetails());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    public List<WorkLog> getWorkLogs(
            WorkType typeFilter,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        List<WorkLog> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM work_log
                WHERE 1=1
                """);

        if (typeFilter != null) {
            sql.append(" AND work_type = ?");
        }

        if (fromDate != null) {
            sql.append(" AND created_at >= ?");
        }

        if (toDate != null) {
            sql.append(" AND created_at <= ?");
        }

        sql.append(" ORDER BY created_at DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;

            if (typeFilter != null) {
                ps.setString(index++, typeFilter.name());
            }

            if (fromDate != null) {
                ps.setTimestamp(index++, Timestamp.valueOf(fromDate.atStartOfDay()));
            }

            if (toDate != null) {
                ps.setTimestamp(index++, Timestamp.valueOf(toDate.atTime(23, 59, 59)));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }


    private WorkLog mapResultSet(ResultSet rs) throws SQLException {

        WorkLog workLog = new WorkLog();

        workLog.setId(rs.getInt("id"));
        workLog.setUsername(rs.getString("username"));
        workLog.setWorkType(WorkType.valueOf(rs.getString("work_type")));
        workLog.setDetails(rs.getString("details"));

        Timestamp ts = rs.getTimestamp("created_at");

        if (ts != null) {
            workLog.setCreatedAt(ts.toLocalDateTime());
        }

        return workLog;
    }
}