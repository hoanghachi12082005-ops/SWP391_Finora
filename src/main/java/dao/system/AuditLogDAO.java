import model.AuditLog;
import util.database.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuditLogDAO {
    
    public boolean insert(AuditLog log) {
        String sql = """
            INSERT INTO audit_log (emp_id, action_name, table_name, record_id, old_data, new_data, created_at)
            VALUES (?, ?, ?, ?, ?, ?, GETDATE())
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, log.getEmpId());
            ps.setString(2, log.getActionName());
            ps.setString(3, log.getTableName());
            ps.setInt(4, log.getRecordId());
            ps.setString(5, log.getOldData());
            ps.setString(6, log.getNewData());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
