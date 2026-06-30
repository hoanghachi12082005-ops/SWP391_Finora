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
<<<<<<< HEAD
        String sql =
                "SELECT DISTINCT " +
                "    e.EmployeeID, " +
                "    e.RoleID, " +
                "    e.BranchID, " +
                "    e.FullName, " +
                "    e.Email, " +
                "    e.Phone, " +
                "    e.Status, " +
                "    e.CreatedAt, " +
                "    b.Name AS BranchName, " +
                "    r.Name AS RoleNames " +
                "FROM Employee e " +
                "LEFT JOIN Branch b ON e.BranchID = b.BranchID " +
                "LEFT JOIN Role r ON e.RoleID = r.RoleID " +
                "WHERE e.EmployeeID = ?";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, employeeID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEmployee(rs);
                }
            }
=======
        try (Connection connection = DBContext.getConnection()) {
            String avatarColumn = resolveAvatarColumn(connection);
            StringBuilder sql = new StringBuilder(
                    "SELECT DISTINCT " +
                    "    e.emp_id, " +
                    "    e.role_id, " +
                    "    e.branch_id, " +
                    "    e.fullName, " +
                    "    e.email, " +
                    "    e.phone, " +
                    "    e.status, " +
                    "    e.created_at, " +
                    "    b.branch_name AS BranchName, " +
                    "    r.role_name AS RoleNames");

            if (avatarColumn != null) {
                sql.append(", e.").append(avatarColumn).append(" AS ImageUrl");
            }

            sql.append(" FROM Employee e " +
                    "LEFT JOIN Branch b ON e.branch_id = b.branch_id " +
                    "LEFT JOIN Role r ON e.role_id = r.role_id " +
                    "WHERE e.emp_id = ?");

            try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
                ps.setInt(1, employeeID);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapEmployee(rs, avatarColumn);
                    }
                }
            }
>>>>>>> cc9c5f8 (Employee Sales Report)
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateProfile(int employeeID, String fullName, String email, String phone, String avatarUrl) {
<<<<<<< HEAD
        String sql =
                "UPDATE Employee " +
                "SET FullName = ?, Email = ?, Phone = ? " +
                "WHERE EmployeeID = ?";

        try (Connection connection = DBContext.getConnection(); 
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setInt(4, employeeID);
=======
        try (Connection connection = DBContext.getConnection()) {
            String avatarColumn = resolveAvatarColumn(connection);
            StringBuilder sql = new StringBuilder(
                    "UPDATE Employee " +
                    "SET fullName = ?, email = ?, phone = ?");

            if (avatarColumn != null) {
                sql.append(", ").append(avatarColumn).append(" = ?");
            }
>>>>>>> cc9c5f8 (Employee Sales Report)

            sql.append(" WHERE emp_id = ?");

            try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
                ps.setString(1, fullName);
                ps.setString(2, email);
                ps.setString(3, phone);

                int index = 4;
                if (avatarColumn != null) {
                    if (avatarUrl == null) {
                        ps.setNull(index, Types.NVARCHAR);
                    } else {
                        ps.setString(index, avatarUrl);
                    }
                    index++;
                }

                ps.setInt(index, employeeID);

                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean isEmailExists(String email, int excludeEmployeeID) {
        String sql =
                "SELECT COUNT(*) AS Total " +
                "FROM Employee " +
<<<<<<< HEAD
                "WHERE Email = ? " +
                "AND EmployeeID <> ?";
=======
                "WHERE email = ? " +
                "AND emp_id <> ?";
>>>>>>> cc9c5f8 (Employee Sales Report)

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
                "SELECT passwordHash " +
                "FROM Employee " +
                "WHERE emp_id = ?";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, employeeID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
<<<<<<< HEAD
                    return rs.getString("PasswordHash");
=======
                    return rs.getString("passwordHash");
>>>>>>> cc9c5f8 (Employee Sales Report)
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
<<<<<<< HEAD
                "SET PasswordHash = ? " +
                "WHERE EmployeeID = ?";
=======
                "SET passwordHash = ? " +
                "WHERE emp_id = ?";
>>>>>>> cc9c5f8 (Employee Sales Report)

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
                "    e.emp_id, " +
                "    e.fullName, " +
                "    b.branch_name AS BranchName, " +
                "    COUNT(o.order_id) AS TotalOrders, " +
                "    COALESCE(SUM(o.total_amount), 0) AS TotalRevenue, " +
                "    COALESCE(AVG(o.total_amount), 0) AS AverageOrderValue " +
                "FROM Employee e " +
                "LEFT JOIN Branch b ON e.branch_id = b.branch_id " +
                "LEFT JOIN [Order] o ON e.emp_id = o.emp_id " +
                "WHERE e.emp_id = ? " +
                "GROUP BY e.emp_id, e.fullName, b.branch_name";

        try (Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql);) {
            ps.setInt(1, employeeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    EmployeeSalesSummary summary = new EmployeeSalesSummary();

                    summary.setEmployeeId(rs.getInt("emp_id"));
                    summary.setFullName(rs.getString("fullName"));
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
                "    e.emp_id, " +
                "    e.fullName, " +
                "    b.branch_name AS BranchName, " +
                "    COUNT(o.order_id) AS TotalOrders, " +
                "    COALESCE(SUM(o.total_amount), 0) AS TotalRevenue, " +
                "    COALESCE(AVG(o.total_amount), 0) AS AverageOrderValue " +
                "FROM Employee e " +
                "LEFT JOIN Branch b ON e.branch_id = b.branch_id " +
                "LEFT JOIN [Order] o ON e.emp_id = o.emp_id AND o.branch_id = ? " +
                "WHERE e.emp_id = ? " +
                "AND e.branch_id = ? " +
                "GROUP BY e.emp_id, e.fullName, b.branch_name";

        try (Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql);) {
            ps.setInt(1, branchId);
            ps.setInt(2, employeeId);
            ps.setInt(3, branchId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    EmployeeSalesSummary summary = new EmployeeSalesSummary();

                    summary.setEmployeeId(rs.getInt("emp_id"));
                    summary.setFullName(rs.getString("fullName"));
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
    private String resolveAvatarColumn(Connection connection) throws SQLException {
        String[] candidateColumns = {"image_URL"};

        DatabaseMetaData metaData = connection.getMetaData();

        for (String candidate : candidateColumns) {
            try (ResultSet columns = metaData.getColumns(null, null, "Employee", candidate)) {
                if (columns.next()) {
                    return candidate;
                }
            }
        }

        return null;
    }

    private Employee mapEmployee(ResultSet rs, String avatarColumn) throws SQLException {
        Employee employee = new Employee();

        employee.setEmployeeID(rs.getInt("emp_id"));
        employee.setRoleID(rs.getInt("role_id"));

        int branchID = rs.getInt("branch_id");

        if (!rs.wasNull()) {
            employee.setBranchID(branchID);
        }
        
<<<<<<< HEAD
        employee.setFullName(rs.getString("FullName"));
        employee.setEmail(rs.getString("Email"));
        employee.setPhone(rs.getString("Phone"));
        employee.setStatus(rs.getString("Status"));
        employee.setCreatedAt(rs.getTimestamp("CreatedAt"));
=======
        employee.setFullName(rs.getString("fullName"));
        employee.setEmail(rs.getString("email"));
        employee.setPhone(rs.getString("phone"));
        employee.setStatus(rs.getString("status"));
        employee.setCreatedAt(rs.getTimestamp("created_at"));
>>>>>>> cc9c5f8 (Employee Sales Report)
        employee.setBranchName(rs.getString("BranchName"));
        employee.setRoleName(rs.getString("RoleNames"));

        if (avatarColumn != null) {
            employee.setAvatarUrl(rs.getString("ImageUrl"));
        }

        return employee;
    }
}
