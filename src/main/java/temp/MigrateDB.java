package temp;

import util.database.DBContext;
import java.sql.Connection;
import java.sql.Statement;

public class MigrateDB {
    public static void main(String[] args) {
        try (Connection conn = DBContext.getConnection();
             Statement stmt = conn.createStatement()) {
            
            String sql = "ALTER TABLE inventory_ticket ADD is_exported_by_sender BIT DEFAULT 0 NOT NULL, is_imported_by_receiver BIT DEFAULT 0 NOT NULL;";
            stmt.executeUpdate(sql);
            System.out.println("Database migrated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
