package dao.system;

import model.ActivityLog;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho Activity Log (bảng AuditLog trong DB V3).
 * READ-ONLY: chỉ truy vấn. Không có insert/update/delete vì audit log
 * là immutable theo nguyên tắc bảo toàn dấu vết hệ thống.
 */
public class ActivityLogDAO {

    // DB V3: AuditLog(AuditLogID, EmployeeID, ActionName, TableName, RecordID, OldData, NewData, CreatedAt)
    // Employee: Employee(EmployeeID, FullName, ...)
    private static final String BASE_SELECT =
            "SELECT a.audit_log_id, a.emp_id, a.action_name, a.table_name, a.record_id, a.old_data, a.new_data, a.created_at, "
          + "       e.fullName AS emp_name, e.branch_id, b.branch_name "
          + "FROM audit_log a LEFT JOIN employee e ON a.emp_id = e.emp_id "
          + "LEFT JOIN branch b ON e.branch_id = b.branch_id ";

    /** Lấy danh sách mới nhất, phục vụ card "Hoạt động gần đây" trên dashboard. */
    public List<ActivityLog> findRecent(int limit) throws SQLException {
        String sql = "SELECT TOP (?) a.audit_log_id, a.emp_id, a.action_name, a.table_name, a.record_id, a.old_data, a.new_data, a.created_at, "
                   + "e.fullName AS emp_name, e.branch_id, b.branch_name "
                   + "FROM audit_log a LEFT JOIN employee e ON a.emp_id = e.emp_id "
                   + "LEFT JOIN branch b ON e.branch_id = b.branch_id "
                   + "ORDER BY a.created_at DESC, a.audit_log_id DESC";
        List<ActivityLog> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extractWithBranch(rs));
            }
        }
        return list;
    }

    public List<ActivityLog> findAll(int offset, int limit, String keyword, String tableName, String actionName,
                                     LocalDate dateFrom, LocalDate dateTo) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1 ");
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (a.ActionName LIKE ? OR a.TableName LIKE ? OR e.FullName LIKE ? OR a.NewData LIKE ?) ");
        }
        if (tableName != null && !tableName.isBlank()) sql.append(" AND a.TableName = ? ");
        if (actionName != null && !actionName.isBlank()) sql.append(" AND a.ActionName = ? ");
        if (dateFrom != null) sql.append(" AND a.CreatedAt >= ? ");
        if (dateTo != null) sql.append(" AND a.CreatedAt < ? ");
        sql.append(" ORDER BY a.CreatedAt DESC, a.AuditLogID DESC ");
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        List<ActivityLog> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword + "%";
                ps.setString(idx++, k);
                ps.setString(idx++, k);
                ps.setString(idx++, k);
                ps.setString(idx++, k);
            }
            if (tableName != null && !tableName.isBlank()) ps.setString(idx++, tableName);
            if (actionName != null && !actionName.isBlank()) ps.setString(idx++, actionName);
            if (dateFrom != null) ps.setTimestamp(idx++, Timestamp.valueOf(dateFrom.atStartOfDay()));
            if (dateTo != null) ps.setTimestamp(idx++, Timestamp.valueOf(dateTo.plusDays(1).atStartOfDay()));
            ps.setInt(idx++, offset);
            ps.setInt(idx, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extractWithBranch(rs));
            }
        }
        return list;
    }

    public int countAll(String keyword, String tableName, String actionName,
                        LocalDate dateFrom, LocalDate dateTo) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM AuditLog a LEFT JOIN Employee e ON a.EmployeeID = e.EmployeeID WHERE 1=1 ");
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (a.ActionName LIKE ? OR a.TableName LIKE ? OR e.FullName LIKE ? OR a.NewData LIKE ?) ");
        }
        if (tableName != null && !tableName.isBlank()) sql.append(" AND a.TableName = ? ");
        if (actionName != null && !actionName.isBlank()) sql.append(" AND a.ActionName = ? ");
        if (dateFrom != null) sql.append(" AND a.CreatedAt >= ? ");
        if (dateTo != null) sql.append(" AND a.CreatedAt < ? ");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword + "%";
                ps.setString(idx++, k);
                ps.setString(idx++, k);
                ps.setString(idx++, k);
                ps.setString(idx++, k);
            }
            if (tableName != null && !tableName.isBlank()) ps.setString(idx++, tableName);
            if (actionName != null && !actionName.isBlank()) ps.setString(idx++, actionName);
            if (dateFrom != null) ps.setTimestamp(idx++, Timestamp.valueOf(dateFrom.atStartOfDay()));
            if (dateTo != null) ps.setTimestamp(idx++, Timestamp.valueOf(dateTo.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public ActivityLog findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE a.AuditLogID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? extract(rs) : null;
            }
        }
    }

    public int countByTableName(String keyword, String tableName, String actionName,
                                LocalDate dateFrom, LocalDate dateTo) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM audit_log a LEFT JOIN employee e ON a.emp_id = e.emp_id WHERE a.table_name = ? ");
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (a.action_name LIKE ? OR a.table_name LIKE ? OR e.fullName LIKE ? OR a.new_data LIKE ?) ");
        }
        if (actionName != null && !actionName.isBlank()) sql.append(" AND a.action_name = ? ");
        if (dateFrom != null) sql.append(" AND a.created_at >= ? ");
        if (dateTo != null) sql.append(" AND a.created_at < ? ");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, tableName);
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword + "%";
                ps.setString(idx++, k);
                ps.setString(idx++, k);
                ps.setString(idx++, k);
                ps.setString(idx++, k);
            }
            if (actionName != null && !actionName.isBlank()) ps.setString(idx++, actionName);
            if (dateFrom != null) ps.setTimestamp(idx++, Timestamp.valueOf(dateFrom.atStartOfDay()));
            if (dateTo != null) ps.setTimestamp(idx++, Timestamp.valueOf(dateTo.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Lấy danh sách tên bảng distinct để hiển thị trong filter. */
    public List<String> findDistinctTables() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT TableName FROM AuditLog WHERE TableName IS NOT NULL ORDER BY TableName";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString(1));
        }
        return list;
    }

    public void insertLog(Integer empId, String actionName, String tableName, Integer recordId,
                          String oldData, String newData) throws SQLException {
        String sql = "INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (empId == null) ps.setNull(1, java.sql.Types.INTEGER); else ps.setInt(1, empId);
            ps.setString(2, actionName);
            ps.setString(3, tableName);
            if (recordId == null) ps.setNull(4, java.sql.Types.INTEGER); else ps.setInt(4, recordId);
            ps.setString(5, oldData);
            ps.setString(6, newData);
            ps.executeUpdate();
        }
    }

    public List<String> findDistinctActions() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT ActionName FROM AuditLog WHERE ActionName IS NOT NULL ORDER BY ActionName";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString(1));
        }
        return list;
    }

    private ActivityLog extract(ResultSet rs) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setId(rs.getInt("AuditLogID"));
        int empId = rs.getInt("EmployeeID");
        if (!rs.wasNull()) log.setEmpId(empId);
        log.setEmpName(rs.getString("EmpName"));
        log.setActionName(rs.getString("ActionName"));
        log.setTableName(rs.getString("TableName"));
        int recordId = rs.getInt("RecordID");
        if (!rs.wasNull()) log.setRecordId(recordId);
        log.setOldData(rs.getString("OldData"));
        log.setNewData(rs.getString("NewData"));
        java.sql.Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) log.setCreatedAt(ts.toLocalDateTime());
        return log;
    }

    private ActivityLog extractWithBranch(ResultSet rs) throws SQLException {
        ActivityLog log = extract(rs);
        int branchId = rs.getInt("branch_id");
        if (!rs.wasNull()) log.setBranchId(branchId);
        log.setBranchName(rs.getString("branch_name"));
        return log;
    }
}
