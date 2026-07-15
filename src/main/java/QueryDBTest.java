import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import util.database.DBContext;

public class QueryDBTest {

    public static void main(String[] args) {
        try {
            System.out.println("=== QUERY EMPLOYEES ===");
            String sqlEmp = "SELECT e.emp_id, e.fullName, e.email, r.role_name, e.status, e.branch_id " +
                            "FROM Employee e " +
                            "LEFT JOIN Role r ON e.role_id = r.role_id " +
                            "WHERE e.fullName LIKE N'%Cường%' OR e.fullName LIKE N'%Dung%'";
            try (Connection conn = DBContext.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlEmp);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("EmpID: %d | Name: %s | Email: %s | Role: %s | BranchID: %d | Status: %s%n",
                            rs.getInt("emp_id"),
                            rs.getString("fullName"),
                            rs.getString("email"),
                            rs.getString("role_name"),
                            rs.getInt("branch_id"),
                            rs.getString("status"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
