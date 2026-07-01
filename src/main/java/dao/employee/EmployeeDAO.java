package dao.employee;

import model.Employee;
import util.database.DBContext;
import java.sql.*;

public class EmployeeDAO {

    private static final int MAX_FAILED = Employee.MAX_FAILED_LOGIN;

    // ─────────────────────────────────────────────────────────
    // TÌM KIẾM
    // ─────────────────────────────────────────────────────────

    /**
     * Tìm nhân viên theo email hoặc số điện thoại, bao gồm FailedLoginCount.
     */
    public Employee findByEmailOrPhone(String username) {
        String sql = "SELECT e.*, r.Name AS RoleName, b.Name AS BranchName "
                + "FROM Employee e "
                + "JOIN Role r ON e.RoleID = r.RoleID "
                + "LEFT JOIN Branch b ON e.BranchID = b.BranchID "
                + "WHERE e.Email = ? OR e.Phone = ?";
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
     * Tăng FailedLoginCount lên 1.
     * Nếu tổng số lần sai >= MAX_FAILED → tự động set Status = 'INACTIVE'.
     *
     * @return số lần đăng nhập sai hiện tại sau khi tăng
     */
    public int incrementFailedAttempts(int employeeId) {
        // Lấy giá trị hiện tại
        int current = getFailedLoginCount(employeeId);
        int newCount = current + 1;

        String sql;
        if (newCount >= MAX_FAILED) {
            // Đủ 5 lần → khoá tài khoản (chuyển INACTIVE)
            sql = "UPDATE Employee "
                + "SET FailedLoginCount = ?, Status = 'INACTIVE', UpdatedAt = CURRENT_TIMESTAMP "
                + "WHERE EmployeeID = ?";
        } else {
            // Chưa đủ → chỉ tăng đếm
            sql = "UPDATE Employee "
                + "SET FailedLoginCount = ?, UpdatedAt = CURRENT_TIMESTAMP "
                + "WHERE EmployeeID = ?";
        }

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newCount);
            ps.setInt(2, employeeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi incrementFailedAttempts: " + e.getMessage());
            e.printStackTrace();
        }
        return newCount;
    }

    /**
     * Reset FailedLoginCount về 0 khi người dùng đăng nhập thành công.
     */
    public void resetFailedAttempts(int employeeId) {
        String sql = "UPDATE Employee "
                   + "SET FailedLoginCount = 0, UpdatedAt = CURRENT_TIMESTAMP "
                   + "WHERE EmployeeID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi resetFailedAttempts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lấy số lần đăng nhập sai hiện tại của tài khoản.
     */
    private int getFailedLoginCount(int employeeId) {
        String sql = "SELECT FailedLoginCount FROM Employee WHERE EmployeeID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("FailedLoginCount");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getFailedLoginCount: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // ─────────────────────────────────────────────────────────
    // CÁC PHƯƠNG THỨC KHÁC
    // ─────────────────────────────────────────────────────────

    public boolean existsByEmail(String email, Object ignore) {
        String sql = "SELECT COUNT(*) FROM Employee WHERE LOWER(Email) = LOWER(?)";
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
     * Insert nhân viên mới vào DB (dùng cho Register).
     */
    public boolean insert(Employee employee) {
        String sql = "INSERT INTO Employee "
                   + "(RoleID, BranchID, FullName, Email, Phone, PasswordHash, Status, CreatedAt, UpdatedAt) "
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
                   + "WHERE LOWER(FullName) = LOWER(?) AND LOWER(Email) = LOWER(?)";
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
                   + "SET PasswordHash = ?, UpdatedAt = CURRENT_TIMESTAMP "
                   + "WHERE LOWER(Email) = LOWER(?)";
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

        // Đọc FailedLoginCount (nếu cột tồn tại trong ResultSet)
        try {
            e.setFailedLoginCount(rs.getInt("FailedLoginCount"));
        } catch (SQLException ex) {
            // Cột chưa được migration → mặc định 0
            e.setFailedLoginCount(0);
        }
        return e;
    }
}
