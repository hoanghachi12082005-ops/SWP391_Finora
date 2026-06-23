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
                "    e.EmployeeID, " +
                "    e.RoleID, " +
                "    e.BranchID, " +
                "    e.FullName, " +
                "    e.Email, " +
                "    e.Phone, " +
                "    e.AvatarUrl, " +
                "    e.Status, " +
                "    e.CreatedAt, " +
                "    b.Name AS BranchName, " +
                "    r.Name AS RoleNames " +
                "FROM Employee e " +
                "LEFT JOIN Branch b ON e.BranchID = b.BranchID " +
                "LEFT JOIN Role r ON e.RoleID = r.RoleID " +
                "WHERE e.EmployeeID = ?";

        try {Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, employeeID);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Employee employee = mapEmployee(rs);

                rs.close();
                ps.close();

                return employee;
            }

            rs.close();
            ps.close();
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
                    "SET FullName = ?, Email = ?, Phone = ? " +
                    "WHERE EmployeeID = ?";
        } else {
            sql =
                    "UPDATE Employee " +
                    "SET FullName = ?, Email = ?, Phone = ?, AvatarUrl = ? " +
                    "WHERE EmployeeID = ?";
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
                "AND EmployeeID <> ?";

        try {Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setString(1, email);
            ps.setInt(2, excludeEmployeeID);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                boolean exists = rs.getInt("Total") > 0;

                rs.close();
                ps.close();

                return exists;
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    public String getPasswordHash(int employeeID) {
        String sql =
                "SELECT PasswordHash " +
                "FROM Employee " +
                "WHERE EmployeeID = ?";

        try {Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, employeeID);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String passwordHash = rs.getString("PasswordHash");

                rs.close();
                ps.close();

                return passwordHash;
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean updatePasswordHash(int employeeID, String newPasswordHash) {
        String sql =
                "UPDATE Employee " +
                "SET PasswordHash = ? " +
                "WHERE EmployeeID = ?";

        try {Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setString(1, newPasswordHash);   
            ps.setInt(2, employeeID);

            boolean success = ps.executeUpdate() > 0;

            ps.close();

            return success;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    public EmployeeSalesSummary getEmployeeSalesSummary(int employeeId) {
        String sql =
                "SELECT " +
                "    e.EmployeeID, " +
                "    e.FullName, " +
                "    b.Name AS BranchName, " +
                "    COUNT(o.OrderID) AS TotalOrders, " +
                "    COALESCE(SUM(o.TotalAmount), 0) AS TotalRevenue, " +
                "    COALESCE(AVG(o.TotalAmount), 0) AS AverageOrderValue " +
                "FROM Employee e " +
                "LEFT JOIN Branch b ON e.BranchID = b.BranchID " +
                "LEFT JOIN [Order] o ON e.EmployeeID = o.EmployeeID " +
                "WHERE e.EmployeeID = ? " +
                "GROUP BY e.EmployeeID, e.FullName, b.Name";

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
                "    e.EmployeeID, " +
                "    e.FullName, " +
                "    b.Name AS BranchName, " +
                "    COUNT(o.OrderID) AS TotalOrders, " +
                "    COALESCE(SUM(o.TotalAmount), 0) AS TotalRevenue, " +
                "    COALESCE(AVG(o.TotalAmount), 0) AS AverageOrderValue " +
                "FROM Employee e " +
                "LEFT JOIN Branch b ON e.BranchID = b.BranchID " +
                "LEFT JOIN [Order] o ON e.EmployeeID = o.EmployeeID AND o.BranchID = ? " +
                "WHERE e.EmployeeID = ? " +
                "AND e.BranchID = ? " +
                "GROUP BY e.EmployeeID, e.FullName, b.Name";

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
        employee.setRoleNames(rs.getString("RoleNames"));

        return employee;
    }
}
