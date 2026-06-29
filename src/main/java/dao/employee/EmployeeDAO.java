package dao.employee;

import model.Employee;
import util.database.DBContext;
import java.sql.*;

public class EmployeeDAO {

    public Employee findByEmailOrPhone(String username) {
        String sql = "SELECT e.*, r.role_name AS RoleName, b.branch_name AS BranchName "
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
                employee.setEmployeeId(rs.getInt("emp_id"));
                employee.setRoleId(rs.getInt("role_id"));
                employee.setBranchId(rs.getObject("branch_id") != null ? rs.getInt("branch_id") : null);
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
        String sql = "SELECT COUNT(*) FROM Employee WHERE LOWER(Email) = LOWER(?)";
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
     * Insert employee mới vào DB (dùng cho Register)
     */
    public boolean insert(Employee employee) {
        String sql = "INSERT INTO Employee (RoleID, BranchID, FullName, Email, Phone, PasswordHash, Status, CreatedAt, UpdatedAt) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = DBContext.getConnection();
            connection.setAutoCommit(true);

            ps = connection.prepareStatement(sql);

            ps.setInt(1, employee.getRoleID());

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

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("LỖI SQL TẠI EmployeeDAO.insert(): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi hệ thống Database: " + e.getMessage());
        } finally {
            if (ps != null) {
                try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (connection != null) {
                try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public boolean checkFullNameAndEmailMatch(String fullName, String email) {
        String sql = "SELECT COUNT(*) FROM Employee WHERE LOWER(FullName) = LOWER(?) AND LOWER(Email) = LOWER(?)";
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
        String sql = "UPDATE Employee SET PasswordHash = ?, UpdatedAt = CURRENT_TIMESTAMP WHERE LOWER(Email) = LOWER(?)";
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

