package dao.system;

import model.ActivityLog;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho Activity Log (b\u1ea3ng audit_log).
 * Cung c\u1ea5p CRUD + ph\u00e2n trang + l\u1ecdc + l\u1ea5y c\u00e1c b\u1ea3n ghi g\u1ea7n nh\u1ea5t cho dashboard.
 */
public class ActivityLogDAO {

    private static final String BASE_SELECT =
            "SELECT a.audit_log_id, a.emp_id, a.action_name, a.table_name, a.record_id, "
          + "       a.old_data, a.new_data, a.created_at, e.fullName AS emp_name "
          + "FROM audit_log a LEFT JOIN employee e ON a.emp_id = e.emp_id ";

    /** L\u1ea5y danh s\u00e1ch m\u1edbi nh\u1ea5t, ph\u1ee5c v\u1ee5 card "Ho\u1ea1t \u0111\u1ed9ng g\u1ea7n \u0111\u00e2y" tr\u00ean dashboard. */
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

    public int insert(ActivityLog log) throws SQLException {
        String sql = "INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, GETDATE())";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getEmpId() > 0) ps.setInt(1, log.getEmpId()); else ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(2, log.getActionName());
            ps.setString(3, log.getTableName());
            if (log.getRecordId() != null) ps.setInt(4, log.getRecordId()); else ps.setNull(4, java.sql.Types.INTEGER);
            ps.setString(5, log.getOldData());
            ps.setString(6, log.getNewData());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    log.setId(newId);
                    return newId;
                }
            }
        }
        return -1;
    }

    public boolean update(ActivityLog log) throws SQLException {
        String sql = "UPDATE audit_log SET emp_id=?, action_name=?, table_name=?, record_id=?, old_data=?, new_data=? "
                   + "WHERE audit_log_id=?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (log.getEmpId() > 0) ps.setInt(1, log.getEmpId()); else ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(2, log.getActionName());
            ps.setString(3, log.getTableName());
            if (log.getRecordId() != null) ps.setInt(4, log.getRecordId()); else ps.setNull(4, java.sql.Types.INTEGER);
            ps.setString(5, log.getOldData());
            ps.setString(6, log.getNewData());
            ps.setInt(7, log.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM audit_log WHERE audit_log_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** L\u1ea5y danh s\u00e1ch t\u00ean b\u1ea3ng distinct \u0111\u1ec3 hi\u1ec3n th\u1ecb trong filter. */
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
