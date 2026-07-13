package dao.employee;

import model.Employee;
import util.database.DBContext;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class EmployeeDAO {

    private static final int MAX_FAILED = Employee.MAX_FAILED_LOGIN;

    // ─────────────────────────────────────────────────────────
    // TÌM KIẾM
    // ─────────────────────────────────────────────────────────

    /**
     * Tìm nhân viên theo email hoặc số điện thoại.
     */
    public Employee findByEmailOrPhone(String username) {
        String sql = "SELECT "
                + "  e.emp_id AS EmployeeID, "
                + "  e.role_id AS RoleID, "
                + "  e.branch_id AS BranchID, "
                + "  e.fullName AS FullName, "
                + "  e.email AS Email, "
                + "  e.phone AS Phone, "
                + "  e.passwordHash AS PasswordHash, "
                + "  e.status AS Status, "
                + "  r.role_name AS RoleName, "
                + "  b.branch_name AS BranchName "
                + "FROM Employee e "
                + "JOIN Role r ON e.role_id = r.role_id "
                + "LEFT JOIN Branch b ON e.branch_id = b.branch_id "
                + "WHERE e.email = ? OR e.phone = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findByEmailOrPhone: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────
    // ĐĂNG NHẬP SAI / KHOÁ TÀI KHOẢN
    // ─────────────────────────────────────────────────────────

    /**
     * Khoá tài khoản nhân viên (đổi status thành INACTIVE).
     */
    public void lockEmployee(int employeeId) {
        String sql = "UPDATE Employee SET status = 'INACTIVE', update_at = CURRENT_TIMESTAMP WHERE emp_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi lockEmployee: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────
    // CÁC PHƯƠNG THỨC KHÁC
    // ─────────────────────────────────────────────────────────

    public boolean existsByEmail(String email, Object ignore) {
        String sql = "SELECT COUNT(*) FROM Employee WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi existsByEmail: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Insert nhân viên mới vào DB 
     */
    public boolean insert(Employee employee) {
        String sql = "INSERT INTO Employee "
                   + "(role_id, branch_id, fullName, email, phone, passwordHash, status, created_at, update_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employee.getRoleID());
            if (employee.getBranchID() != null && employee.getBranchID() > 0) {
                ps.setInt(2, employee.getBranchID());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, employee.getFullName());
            ps.setString(4, employee.getEmail());
            ps.setString(5, employee.getPhone());
            ps.setString(6, employee.getPasswordHash());
            ps.setString(7, employee.getStatus() != null ? employee.getStatus() : "ACTIVE");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("LỖI SQL TẠI EmployeeDAO.insert(): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi hệ thống Database: " + e.getMessage());
        }
    }

    public boolean checkFullNameAndEmailMatch(String fullName, String email) {
        String sql = "SELECT COUNT(*) FROM Employee "
                   + "WHERE LOWER(fullName) = LOWER(?) AND LOWER(email) = LOWER(?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi checkFullNameAndEmailMatch: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePasswordByEmail(String email, String newPasswordHash) {
        String sql = "UPDATE Employee "
                   + "SET passwordHash = ?, update_at = CURRENT_TIMESTAMP "
                   + "WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi updatePasswordByEmail: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────
    // PHƯƠNG THỨC COMPATIBILITY CHO BRANCH CONTROLLER
    // ─────────────────────────────────────────────────────────

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM employee";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countByBranch(int branchId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM employee WHERE branch_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public List<Employee> getByBranch(int branchId) throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT e.emp_id AS EmployeeID, e.role_id AS RoleID, e.branch_id AS BranchID, "
                   + "e.fullName AS FullName, e.email AS Email, e.phone AS Phone, e.passwordHash AS PasswordHash, "
                   + "e.status AS Status, r.role_name AS RoleName, "
                   + "b.branch_name AS BranchName "
                   + "FROM employee e "
                   + "JOIN Role r ON e.role_id = r.role_id "
                   + "LEFT JOIN Branch b ON e.branch_id = b.branch_id "
                   + "WHERE e.branch_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Employee> getAll() throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT e.emp_id AS EmployeeID, e.role_id AS RoleID, e.branch_id AS BranchID, "
                   + "e.fullName AS FullName, e.email AS Email, e.phone AS Phone, e.passwordHash AS PasswordHash, "
                   + "e.status AS Status, r.role_name AS RoleName, "
                   + "b.branch_name AS BranchName "
                   + "FROM employee e "
                   + "JOIN Role r ON e.role_id = r.role_id "
                   + "LEFT JOIN Branch b ON e.branch_id = b.branch_id";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────────────────────

    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setEmployeeId(rs.getInt("EmployeeID"));
        e.setRoleId(rs.getInt("RoleID"));
        e.setBranchId(rs.getObject("BranchID") != null ? rs.getInt("BranchID") : null);
        e.setFullName(rs.getString("FullName"));
        e.setEmail(rs.getString("Email"));
        e.setPhone(rs.getString("Phone"));
        String hash = rs.getString("PasswordHash");
        e.setPasswordHash(hash != null ? hash.trim() : null);
        e.setStatus(rs.getString("Status"));
        e.setRoleName(rs.getString("RoleName"));
        e.setBranchName(rs.getString("BranchName"));

        return e;
    }
}
