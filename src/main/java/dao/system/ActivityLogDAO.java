package dao.system;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import util.database.DBContext;

public class ActivityLogDAO {

    public void log(Integer empId, String actionName, String tableName, Integer recordId, String oldData, String newData) {
        String sql = "INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, GETDATE())";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (empId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, empId);
            }
            ps.setString(2, actionName);
            ps.setString(3, tableName);
            if (recordId == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, recordId);
            }
            ps.setString(5, oldData);
            ps.setString(6, newData);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("AuditLog error: " + e.getMessage());
        }
    }
}
