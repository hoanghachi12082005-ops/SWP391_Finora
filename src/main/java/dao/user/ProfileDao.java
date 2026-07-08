/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao.user;

/**
 *
 * @author PCQN
 */
import model.Employee;
import util.database.DBContext;

import java.sql.*;
import model.EmployeeSalesSummary;

public class ProfileDao extends DBContext {

    public Employee getProfileById(int employeeID) {
        String sql =
                "SELECT DISTINCT " +
                "    e.emp_id AS EmployeeID, " +
                "    e.role_id AS RoleID, " +
                "    e.branch_id AS BranchID, " +
                "    e.FullName, " +
                "    e.Email, " +
                "    e.Phone, " +
                "    e.image_URL AS AvatarUrl, " +
                "    e.Status, " +
                "    e.created_at AS CreatedAt, " +
                "    b.branch_name AS BranchName, " +
                "    r.role_name AS RoleNames " +
                "FROM Employee e " +
                "LEFT JOIN Branch b ON e.branch_id = b.branch_id " +
                "LEFT JOIN Role r ON e.role_id = r.role_id " +
                "WHERE e.emp_id = ?";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, employeeID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEmployee(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateProfile(int employeeID, String fullName, String email, String phone, String avatarUrl) {
        String sql;

        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            sql =
                    "UPDATE Employee " +
                    "SET FullName = ?, Email = ?, Phone = ?, update_at = GETDATE() " +
                    "WHERE emp_id = ?";
        } else {
            sql =
                    "UPDATE Employee " +
                    "SET FullName = ?, Email = ?, Phone = ?, image_URL = ?, update_at = GETDATE() " +
                    "WHERE emp_id = ?";
        }

        try (Connection connection = DBContext.getConnection(); 
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, phone);

            if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                ps.setInt(4, employeeID);
            } else {
                ps.setString(4, avatarUrl);
                ps.setInt(5, employeeID);
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean isEmailExists(String email, int excludeEmployeeID) {
        String sql =
                "SELECT COUNT(*) AS Total " +
                "FROM Employee " +
                "WHERE Email = ? " +
                "AND emp_id <> ?";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setInt(2, excludeEmployeeID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Total") > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    public String getPasswordHash(int employeeID) {
        String sql =
                "SELECT PasswordHash " +
                "FROM Employee " +
                "WHERE emp_id = ?";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, employeeID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("PasswordHash");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean updatePasswordHash(int employeeID, String newPasswordHash) {
        String sql =
                "UPDATE Employee " +
                "SET PasswordHash = ?, update_at = GETDATE() " +
                "WHERE emp_id = ?";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, newPasswordHash);   
            ps.setInt(2, employeeID);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    public EmployeeSalesSummary getEmployeeSalesSummary(int employeeId) {
        String sql =
                "SELECT " +
                "    e.emp_id AS EmployeeID, " +
                "    e.FullName, " +
                "    b.branch_name AS BranchName, " +
                "    COUNT(o.order_id) AS TotalOrders, " +
                "    COALESCE(SUM(o.total_amount), 0) AS TotalRevenue, " +
                "    COALESCE(AVG(o.total_amount), 0) AS AverageOrderValue " +
                "FROM Employee e " +
                "LEFT JOIN Branch b ON e.branch_id = b.branch_id " +
                "LEFT JOIN [Order] o ON e.emp_id = o.emp_id " +
                "WHERE e.emp_id = ? " +
                "GROUP BY e.emp_id, e.FullName, b.branch_name";

        try (Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql);) {
            ps.setInt(1, employeeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    EmployeeSalesSummary summary = new EmployeeSalesSummary();

                    summary.setEmployeeId(rs.getInt("EmployeeID"));
                    summary.setFullName(rs.getString("FullName"));
                    summary.setBranchName(rs.getString("BranchName"));
                    summary.setTotalOrders(rs.getInt("TotalOrders"));
                    summary.setTotalRevenue(rs.getBigDecimal("TotalRevenue"));
                    summary.setAverageOrderValue(rs.getBigDecimal("AverageOrderValue"));

                    return summary;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new EmployeeSalesSummary();
    }
    public EmployeeSalesSummary getEmployeeSalesSummaryInBranch(int employeeId, int branchId) {
        String sql =
                "SELECT " +
                "    e.emp_id AS EmployeeID, " +
                "    e.FullName, " +
                "    b.branch_name AS BranchName, " +
                "    COUNT(o.order_id) AS TotalOrders, " +
                "    COALESCE(SUM(o.total_amount), 0) AS TotalRevenue, " +
                "    COALESCE(AVG(o.total_amount), 0) AS AverageOrderValue " +
                "FROM Employee e " +
                "LEFT JOIN Branch b ON e.branch_id = b.branch_id " +
                "LEFT JOIN [Order] o ON e.emp_id = o.emp_id AND o.branch_id = ? " +
                "WHERE e.emp_id = ? " +
                "AND e.branch_id = ? " +
                "GROUP BY e.emp_id, e.FullName, b.branch_name";

        try (Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql);) {
            ps.setInt(1, branchId);
            ps.setInt(2, employeeId);
            ps.setInt(3, branchId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    EmployeeSalesSummary summary = new EmployeeSalesSummary();

                    summary.setEmployeeId(rs.getInt("EmployeeID"));
                    summary.setFullName(rs.getString("FullName"));
                    summary.setBranchName(rs.getString("BranchName"));
                    summary.setTotalOrders(rs.getInt("TotalOrders"));
                    summary.setTotalRevenue(rs.getBigDecimal("TotalRevenue"));
                    summary.setAverageOrderValue(rs.getBigDecimal("AverageOrderValue"));

                    return summary;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new EmployeeSalesSummary();
    }
    private Employee mapEmployee(ResultSet rs) throws SQLException {
        Employee employee = new Employee();

        employee.setEmployeeID(rs.getInt("EmployeeID"));
        employee.setRoleID(rs.getInt("RoleID"));

        int branchID = rs.getInt("BranchID");

        if (!rs.wasNull()) {
            employee.setBranchID(branchID);
        }
        
        employee.setFullName(rs.getString("FullName"));
        employee.setEmail(rs.getString("Email"));
        employee.setPhone(rs.getString("Phone"));
        employee.setStatus(rs.getString("Status"));
        employee.setAvatarUrl(rs.getString("AvatarUrl"));
        employee.setCreatedAt(rs.getTimestamp("CreatedAt"));
        employee.setBranchName(rs.getString("BranchName"));
        employee.setRoleName(rs.getString("RoleNames"));
        employee.setRoleNames(rs.getString("RoleNames"));

        return employee;
    }
}
