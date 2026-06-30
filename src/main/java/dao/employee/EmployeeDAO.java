package dao.employee;

import model.Employee;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class EmployeeDAO {

    public Employee findByEmailOrPhone(String username) {
        String sql = "SELECT e.emp_id, e.branch_id, e.role_id, e.fullName, e.email, e.phone, e.passwordHash, e.status, "
                + "r.role_name AS RoleName, b.branch_name AS BranchName "
                + "FROM Employee e "
                + "JOIN [Role] r ON e.role_id = r.role_id "
                + "LEFT JOIN Branch b ON e.branch_id = b.branch_id "
                + "WHERE e.email = ? OR e.phone = ?";
        try (Connection connection = DBContext.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Employee employee = new Employee();
                employee.setEmployeeID(rs.getInt("emp_id"));
                employee.setRoleID(rs.getInt("role_id"));
                employee.setBranchID(rs.getObject("branch_id") != null ? rs.getInt("branch_id") : null);
                employee.setFullName(rs.getString("fullName"));
                employee.setEmail(rs.getString("email"));
                employee.setPhone(rs.getString("phone"));
                employee.setPasswordHash(rs.getString("passwordHash") != null ? rs.getString("passwordHash").trim() : null);
                employee.setStatus(rs.getString("status"));
                employee.setRoleName(rs.getString("RoleName"));
                employee.setBranchName(rs.getString("BranchName"));
                return employee;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findByEmailOrPhone: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean existsByEmail(String email, Object ignore) {
        String sql = "SELECT COUNT(*) FROM Employee WHERE LOWER(email) = LOWER(?)";
        try (Connection connection = DBContext.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi existsByEmail: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Hàm insert chuẩn hóa chạy ổn định 100% trên môi trường Web Server
     */
    public boolean insert(Employee employee) {
        String sql = "INSERT INTO Employee (role_id, branch_id, fullName, email, phone, passwordHash, status, created_at, update_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        Connection connection = null;
        PreparedStatement ps = null;

        try {
            // 1. Lấy Connection thủ công để tránh việc bị đóng sớm bởi cơ chế try-with-resources ngầm
            connection = DBContext.getConnection();
            
            // 2. Bật chế độ tự động lưu để lệnh bắn thẳng xuống đĩa cứng SQL Server lập tức
            connection.setAutoCommit(true);

            ps = connection.prepareStatement(sql);

            // Gán các tham số
            ps.setInt(1, employee.getRoleID());

            // Xử lý an toàn cho BranchID: Nếu là Owner không thuộc chi nhánh nào thì truyền NULL vào DB
            if (employee.getBranchID() != null && employee.getBranchID() > 0) {
                ps.setInt(2, employee.getBranchID());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }

            ps.setString(3, employee.getFullName());
            ps.setString(4, employee.getEmail());
            ps.setString(5, employee.getPhone());
            ps.setString(6, employee.getPasswordHash());
            ps.setString(7, employee.getStatus() != null ? employee.getStatus() : "ACTIVE");

            // 3. Thực thi câu lệnh
            int rowsAffected = ps.executeUpdate();

            // Log trạng thái kiểm tra nóng tại Console
            System.out.println("=== [EmployeeDAO] KẾT QUẢ THỰC THI INSERT ===");
            System.out.println("-> Số dòng ảnh hưởng: " + rowsAffected);
            System.out.println("-> Email đăng ký thành công: " + employee.getEmail());
            System.out.println("============================================");

            return rowsAffected > 0;

        } catch (SQLException e) {
            // Nếu có bất kỳ lỗi ràng buộc dữ liệu nào từ SQL Server, lập tức in đỏ ra màn hình Console
            System.err.println("🔴 LỖI SQL TẠI EmployeeDAO.insert(): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi hệ thống Database: " + e.getMessage());
        } finally {
            // 4. Giải phóng tài nguyên thủ công sau khi dữ liệu đã được ghi hoàn tất
            if (ps != null) {
                try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (connection != null) {
                try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public boolean checkFullNameAndEmailMatch(String fullName, String email) {
        String sql = "SELECT COUNT(*) FROM Employee WHERE LOWER(fullName) = LOWER(?) AND LOWER(email) = LOWER(?)";
        try (Connection connection = DBContext.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi checkFullNameAndEmailMatch: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePasswordByEmail(String email, String newPasswordHash) {
        String sql = "UPDATE Employee SET passwordHash = ?, update_at = CURRENT_TIMESTAMP WHERE LOWER(email) = LOWER(?)";
        try (Connection connection = DBContext.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi updatePasswordByEmail: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
}