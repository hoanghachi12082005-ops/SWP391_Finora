package dao.user;

import model.Employee;
import model.EmployeeSalesSummary;
import util.database.DBContext;
import java.sql.*;

public class ProfileDao {

    public Employee getProfileById(int employeeID) {
        String sql =
                "SELECT DISTINCT " +
                "    e.emp_id, " +
                "    e.role_id, " +
                "    e.branch_id, " +
                "    e.fullName, " +
                "    e.email, " +
                "    e.phone, " +
                "    e.status, " +
                "    e.created_at, " +
                "    e.image_URL, " +
                "    b.branch_name AS BranchName, " +
                "    r.role_name AS RoleNames " +
                "FROM Employee e " +
                "LEFT JOIN Branch b ON e.branch_id = b.branch_id " +
                "LEFT JOIN [Role] r ON e.role_id = r.role_id " +
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
        String sql =
                "UPDATE Employee " +
                "SET fullName = ?, email = ?, phone = ?, image_URL = ? " +
                "WHERE emp_id = ?";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, phone);
            if (avatarUrl == null) {
                ps.setNull(4, Types.NVARCHAR);
            } else {
                ps.setString(4, avatarUrl);
            }
            ps.setInt(5, employeeID);
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
                "WHERE email = ? " +
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
                "SELECT passwordHash " +
                "FROM Employee " +
                "WHERE emp_id = ?";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, employeeID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("passwordHash");
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
                "SET passwordHash = ? " +
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
             PreparedStatement ps = connection.prepareStatement(sql)) {
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
             PreparedStatement ps = connection.prepareStatement(sql)) {
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

    private Employee mapEmployee(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setEmployeeID(rs.getInt("emp_id"));
        employee.setRoleID(rs.getInt("role_id"));

        int branchID = rs.getInt("branch_id");
        if (!rs.wasNull()) {
            employee.setBranchID(branchID);
        }

        employee.setFullName(rs.getString("fullName"));
        employee.setEmail(rs.getString("email"));
        employee.setPhone(rs.getString("phone"));
        employee.setStatus(rs.getString("status"));
        employee.setCreatedAt(rs.getTimestamp("created_at"));
        employee.setBranchName(rs.getString("BranchName"));
        employee.setRoleName(rs.getString("RoleNames"));

        String imageUrl = rs.getString("image_URL");
        if (imageUrl != null) {
            employee.setAvatarUrl(imageUrl);
        }

        return employee;
    }
}
