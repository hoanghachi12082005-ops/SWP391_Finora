package dao.employee;

import model.Employee;
import util.database.DBContext;
import java.sql.*;

public class EmployeeDAO {

    public Employee findByEmailOrPhone(String username) {
        String sql = "SELECT e.*, r.Name AS RoleName, b.Name AS BranchName "
                + "FROM Employee e "
                + "JOIN Role r ON e.RoleID = r.RoleID "
                + "LEFT JOIN Branch b ON e.BranchID = b.BranchID "
                + "WHERE e.Email = ? OR e.Phone = ?";
        try (Connection connection = DBContext.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Employee employee = new Employee();
                employee.setEmployeeId(rs.getInt("EmployeeID"));
                employee.setRoleId(rs.getInt("RoleID"));
                employee.setBranchId(rs.getObject("BranchID") != null ? rs.getInt("BranchID") : null);
                employee.setFullName(rs.getString("FullName"));
                employee.setEmail(rs.getString("Email"));
                employee.setPhone(rs.getString("Phone"));
                employee.setPasswordHash(rs.getString("PasswordHash") != null ? rs.getString("PasswordHash").trim() : null);
                employee.setStatus(rs.getString("Status"));
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

//    public boolean existsByEmail(String email, String phone, Object ignore) {
//        String sql = "SELECT COUNT(*) FROM Employee WHERE LOWER(Email) = LOWER(?) OR Phone= ?";
//        try (Connection connection = DBContext.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
//            ps.setString(1, email);
//            ps.setString(2, phone);
//            ResultSet rs = ps.executeQuery();
//            if (rs.next()) {
//                return rs.getInt(1) > 0;
//                
//            }
//        } catch (SQLException e) {
//            System.err.println("Lỗi existsByEmail: " + e.getMessage());
//            e.printStackTrace();
//        }
//        return false;
//    }


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
