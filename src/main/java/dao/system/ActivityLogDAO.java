package dao.system;

import model.ActivityLog;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho Activity Log (bảng audit_log).
 * READ-ONLY: chỉ truy vấn. Không có insert/update/delete vì audit log
 * là immutable theo nguyên tắc bảo toàn dấu vết hệ thống.
 * Việc ghi log do trigger DB / service nội bộ phụ trách, không qua DAO này.
 */
public class ActivityLogDAO {

    private static final String BASE_SELECT =
            "SELECT a.audit_log_id, a.emp_id, a.action_name, a.table_name, a.record_id, "
          + "       a.old_data, a.new_data, a.created_at, e.fullName AS emp_name "
          + "FROM audit_log a LEFT JOIN employee e ON a.emp_id = e.emp_id ";

    /** Lấy danh sách mới nhất, phục vụ card "Hoạt động gần đây" trên dashboard. */
    public List<ActivityLog> findRecent(int limit) throws SQLException {
        String sql = "SELECT TOP (?) a.audit_log_id, a.emp_id, a.action_name, a.table_name, a.record_id, "
                   + "a.old_data, a.new_data, a.created_at, e.fullName AS emp_name "
                   + "FROM audit_log a LEFT JOIN employee e ON a.emp_id = e.emp_id "
                   + "ORDER BY a.created_at DESC, a.audit_log_id DESC";
        List<ActivityLog> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    public List<ActivityLog> findAll(int offset, int limit, String keyword, String tableName, String actionName)
            throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1 ");
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (a.action_name LIKE ? OR a.table_name LIKE ? OR e.fullName LIKE ? OR a.new_data LIKE ?) ");
        }
        if (tableName != null && !tableName.isBlank()) sql.append(" AND a.table_name = ? ");
        if (actionName != null && !actionName.isBlank()) sql.append(" AND a.action_name = ? ");
        sql.append(" ORDER BY a.created_at DESC, a.audit_log_id DESC ");
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
            ps.setInt(idx++, offset);
            ps.setInt(idx, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extract(rs));
            }
        }
        return list;
    }

    public int countAll(String keyword, String tableName, String actionName) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM audit_log a LEFT JOIN employee e ON a.emp_id = e.emp_id WHERE 1=1 ");
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (a.action_name LIKE ? OR a.table_name LIKE ? OR e.fullName LIKE ? OR a.new_data LIKE ?) ");
        }
        if (tableName != null && !tableName.isBlank()) sql.append(" AND a.table_name = ? ");
        if (actionName != null && !actionName.isBlank()) sql.append(" AND a.action_name = ? ");

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
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public ActivityLog findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE a.audit_log_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? extract(rs) : null;
            }
        }
    }

    /** Lấy danh sách tên bảng distinct để hiển thị trong filter. */
    public List<String> findDistinctTables() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT table_name FROM audit_log WHERE table_name IS NOT NULL ORDER BY table_name";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString(1));
        }
        return list;
    }

    public List<String> findDistinctActions() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT action_name FROM audit_log WHERE action_name IS NOT NULL ORDER BY action_name";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString(1));
        }
        return list;
    }

    private ActivityLog extract(ResultSet rs) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setId(rs.getInt("audit_log_id"));
        int empId = rs.getInt("emp_id");
        if (!rs.wasNull()) log.setEmpId(empId);
        log.setEmpName(rs.getString("emp_name"));
        log.setActionName(rs.getString("action_name"));
        log.setTableName(rs.getString("table_name"));
        int recordId = rs.getInt("record_id");
        if (!rs.wasNull()) log.setRecordId(recordId);
        log.setOldData(rs.getString("old_data"));
        log.setNewData(rs.getString("new_data"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) log.setCreatedAt(ts.toLocalDateTime());
        return log;
    }
}
