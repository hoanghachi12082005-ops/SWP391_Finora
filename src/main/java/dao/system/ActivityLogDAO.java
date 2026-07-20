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
import java.util.Collections;
import java.util.List;

/**
 * DAO cho Activity Log (bảng AuditLog).
 * Dùng Keyset Pagination thuần (audit_log_id) — không OFFSET, không số trang.
 */
public class ActivityLogDAO {

    private static final String BASE_SELECT =
            "SELECT a.audit_log_id, a.emp_id, a.action_name, a.table_name, a.record_id, a.old_data, a.new_data, a.created_at, "
          + "       e.fullName AS emp_name, e.branch_id, b.branch_name "
          + "FROM audit_log a LEFT JOIN employee e ON a.emp_id = e.emp_id "
          + "LEFT JOIN branch b ON e.branch_id = b.branch_id ";

    /** Lấy danh sách mới nhất cho card "Hoạt động gần đây" trên dashboard. */
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

    /**
     * Keyset pagination thuần.
     *
     * @param beforeId  null ở trang đầu → lấy mới nhất.
     *                  != null → lấy các bản ghi cũ hơn beforeId (ORDER BY DESC, WHERE id < beforeId).
     * @param afterId   != null → lấy các bản ghi mới hơn afterId (ORDER BY ASC, WHERE id > afterId, reverse trong Java).
     * @param limit     số bản ghi trả về. Hai query index seek riêng (existsLessThan / existsGreaterThan)
     *                  được dùng để xác định hasNext / hasPrev thay vì limit+1.
     */
    public List<ActivityLog> findByKeyset(Integer beforeId, Integer afterId, int limit,
                                          String keyword, String tableName, String actionName,
                                          LocalDate dateFrom, LocalDate dateTo) throws SQLException {
        boolean isPrev = (afterId != null);

        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1 ");

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (a.action_name LIKE ? OR a.table_name LIKE ? OR e.fullName LIKE ? OR a.new_data LIKE ?) ");
        }
        if (tableName != null && !tableName.isBlank()) sql.append(" AND a.table_name = ? ");
        if (actionName != null && !actionName.isBlank()) sql.append(" AND a.action_name = ? ");
        if (dateFrom != null) sql.append(" AND a.created_at >= ? ");
        if (dateTo != null) sql.append(" AND a.created_at < ? ");

        if (beforeId != null) {
            sql.append(" AND a.audit_log_id < ? ");
        } else if (afterId != null) {
            sql.append(" AND a.audit_log_id > ? ");
        }

        sql.append(isPrev ? " ORDER BY a.audit_log_id ASC " : " ORDER BY a.audit_log_id DESC ");
        sql.append(" OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY");

        List<ActivityLog> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword + "%";
                for (int i = 0; i < 4; i++) ps.setString(idx++, k);
            }
            if (tableName != null && !tableName.isBlank()) ps.setString(idx++, tableName);
            if (actionName != null && !actionName.isBlank()) ps.setString(idx++, actionName);
            if (dateFrom != null) ps.setTimestamp(idx++, Timestamp.valueOf(dateFrom.atStartOfDay()));
            if (dateTo != null) ps.setTimestamp(idx++, Timestamp.valueOf(dateTo.plusDays(1).atStartOfDay()));
            if (beforeId != null) ps.setInt(idx++, beforeId);
            else if (afterId != null) ps.setInt(idx++, afterId);
            ps.setInt(idx, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extractWithBranch(rs));
            }
        }

        if (isPrev) Collections.reverse(list);
        return list;
    }

    /**
     * Kiểm tra xem có bản ghi nào với audit_log_id > givenId không (dùng cho hasPrev).
     * SELECT TOP 1 1 → index seek, rất nhẹ.
     */
    public boolean existsGreaterThan(int id, String keyword, String tableName,
                                     String actionName, LocalDate dateFrom, LocalDate dateTo) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT TOP 1 1 FROM audit_log a LEFT JOIN employee e ON a.emp_id = e.emp_id WHERE a.audit_log_id > ? ");
        appendWhere(sql, keyword, tableName, actionName, dateFrom, dateTo);
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setInt(idx++, id);
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword + "%";
                for (int i = 0; i < 4; i++) ps.setString(idx++, k);
            }
            if (tableName != null && !tableName.isBlank()) ps.setString(idx++, tableName);
            if (actionName != null && !actionName.isBlank()) ps.setString(idx++, actionName);
            if (dateFrom != null) ps.setTimestamp(idx++, Timestamp.valueOf(dateFrom.atStartOfDay()));
            if (dateTo != null) ps.setTimestamp(idx++, Timestamp.valueOf(dateTo.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Kiểm tra xem có bản ghi nào với audit_log_id < givenId không (dùng cho hasNext).
     * SELECT TOP 1 1 → index seek, rất nhẹ.
     */
    public boolean existsLessThan(int id, String keyword, String tableName,
                                  String actionName, LocalDate dateFrom, LocalDate dateTo) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT TOP 1 1 FROM audit_log a LEFT JOIN employee e ON a.emp_id = e.emp_id WHERE a.audit_log_id < ? ");
        appendWhere(sql, keyword, tableName, actionName, dateFrom, dateTo);
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setInt(idx++, id);
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword + "%";
                for (int i = 0; i < 4; i++) ps.setString(idx++, k);
            }
            if (tableName != null && !tableName.isBlank()) ps.setString(idx++, tableName);
            if (actionName != null && !actionName.isBlank()) ps.setString(idx++, actionName);
            if (dateFrom != null) ps.setTimestamp(idx++, Timestamp.valueOf(dateFrom.atStartOfDay()));
            if (dateTo != null) ps.setTimestamp(idx++, Timestamp.valueOf(dateTo.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Đếm tổng số log để hiển thị (không liên quan pagination). */
    public int countAll(String keyword, String tableName, String actionName,
                        LocalDate dateFrom, LocalDate dateTo) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM audit_log a LEFT JOIN employee e ON a.emp_id = e.emp_id WHERE 1=1 ");
        appendWhere(sql, keyword, tableName, actionName, dateFrom, dateTo);
        return count(sql.toString(), keyword, tableName, actionName, dateFrom, dateTo);
    }

    public int countByTableName(String keyword, String tableName, String actionName,
                                LocalDate dateFrom, LocalDate dateTo) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM audit_log a LEFT JOIN employee e ON a.emp_id = e.emp_id WHERE 1=1 ");
        appendWhere(sql, keyword, tableName, actionName, dateFrom, dateTo);
        return count(sql.toString(), keyword, tableName, actionName, dateFrom, dateTo);
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

    public List<String> findDistinctTables() throws SQLException {
        List<String> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT DISTINCT table_name FROM audit_log WHERE table_name IS NOT NULL ORDER BY table_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString(1));
        }
        return list;
    }

    public List<String> findDistinctActions() throws SQLException {
        List<String> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT DISTINCT action_name FROM audit_log WHERE action_name IS NOT NULL ORDER BY action_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString(1));
        }
        return list;
    }

    // ==================== PRIVATE HELPERS ====================

    private void appendWhere(StringBuilder sql, String keyword, String tableName,
                             String actionName, LocalDate dateFrom, LocalDate dateTo) {
        if (keyword != null && !keyword.isBlank())
            sql.append(" AND (a.action_name LIKE ? OR a.table_name LIKE ? OR e.fullName LIKE ? OR a.new_data LIKE ?) ");
        if (tableName != null && !tableName.isBlank()) sql.append(" AND a.table_name = ? ");
        if (actionName != null && !actionName.isBlank()) sql.append(" AND a.action_name = ? ");
        if (dateFrom != null) sql.append(" AND a.created_at >= ? ");
        if (dateTo != null) sql.append(" AND a.created_at < ? ");
    }

    private int count(String sql, String keyword, String tableName,
                      String actionName, LocalDate dateFrom, LocalDate dateTo) throws SQLException {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword + "%";
                for (int i = 0; i < 4; i++) ps.setString(idx++, k);
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
        java.sql.Timestamp ts = rs.getTimestamp("created_at");
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
