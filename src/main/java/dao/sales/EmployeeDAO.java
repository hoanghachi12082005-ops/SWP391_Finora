package dao.sales;

import model.Employee;
import model.Employee.EmployeeStatus;
import util.database.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO xử lý các thao tác CRUD cho bảng Employee.
 *
 * @author Finora Team
 */
public class EmployeeDAO {

    // =========================================================
    //  SQL Constants
    // =========================================================

    private static final String SELECT_ALL =
        "SELECT emp_id, branch_id, role_id, fullName, gender, bod, address, email, " +
        "phone, passwordHash, status, created_at, update_at FROM employee";

    private static final String SELECT_BY_ID =
        SELECT_ALL + " WHERE emp_id = ?";

    private static final String SELECT_BY_BRANCH =
        SELECT_ALL + " WHERE branch_id = ?";

    private static final String SELECT_BY_EMAIL =
        SELECT_ALL + " WHERE email = ?";

    private static final String INSERT =
        "INSERT INTO employee (branch_id, role_id, fullName, gender, bod, address, email, " +
        "phone, passwordHash, status) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
        "UPDATE employee SET branch_id=?, role_id=?, fullName=?, gender=?, bod=?, address=?, " +
        "email=?, phone=?, status=?, update_at=GETDATE() WHERE emp_id=?";

    private static final String UPDATE_PASSWORD =
        "UPDATE employee SET passwordHash=?, update_at=GETDATE() WHERE emp_id=?";

    private static final String DELETE =
        "DELETE FROM employee WHERE emp_id=?";

    private static final String UPDATE_STATUS =
        "UPDATE employee SET status=?, update_at=GETDATE() WHERE emp_id=?";

    private static final String SELECT_BY_STATUS =
        SELECT_ALL + " WHERE status = ?";

    private static final String SEARCH =
        SELECT_ALL + " WHERE fullName LIKE ? OR email LIKE ? OR phone LIKE ?";

    private static final String COUNT_BY_BRANCH =
        "SELECT COUNT(*) FROM employee WHERE branch_id = ?";

    private static final String COUNT_ALL =
        "SELECT COUNT(*) FROM employee";

    // =========================================================
    //  Mapping Helper
    // =========================================================

    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setEmpId(rs.getInt("emp_id"));
        e.setBranchId(rs.getInt("branch_id"));
        e.setFullName(rs.getString("fullName"));
        e.setGender(rs.getString("gender"));
        e.setBod(rs.getString("bod"));
        e.setAddress(rs.getString("address"));
        e.setEmail(rs.getString("email"));
        e.setPhone(rs.getString("phone"));
        e.setPasswordHash(rs.getString("passwordHash"));
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                e.setStatus(EmployeeStatus.valueOf(statusStr.toUpperCase()));
            } catch (IllegalArgumentException ex) {
                e.setStatus(EmployeeStatus.ACTIVE);
            }
        }
        e.setCreatedAt(rs.getString("created_at"));
        e.setUpdateAt(rs.getString("update_at"));
        return e;
    }

    // =========================================================
    //  CRUD Operations
    // =========================================================

    /** Lấy tất cả nhân viên. */
    public List<Employee> getAll() throws SQLException {
        List<Employee> list = new ArrayList<>();
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Lấy nhân viên theo ID. */
    public Employee getById(int empId) throws SQLException {
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Lấy danh sách nhân viên theo chi nhánh. */
    public List<Employee> getByBranch(int branchId) throws SQLException {
        List<Employee> list = new ArrayList<>();
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_BRANCH)) {
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Lấy nhân viên theo email (dùng cho đăng nhập). */
    public Employee getByEmail(String email) throws SQLException {
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_EMAIL)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Thêm nhân viên mới. Trả về emp_id được sinh ra, hoặc -1 nếu thất bại. */
    public int insert(Employee emp) throws SQLException {
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, emp.getBranchId());
            ps.setInt(2, emp.getRoleId());          // role_id NOT NULL
            ps.setString(3, emp.getFullName());
            ps.setString(4, emp.getGender());
            ps.setString(5, emp.getBod());
            ps.setString(6, emp.getAddress());
            ps.setString(7, emp.getEmail());
            ps.setString(8, emp.getPhone());
            ps.setString(9, emp.getPasswordHash());
            ps.setString(10, emp.getStatus() != null ? emp.getStatus().name() : "ACTIVE");
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    /** Cập nhật thông tin nhân viên (không đổi mật khẩu). Trả về true nếu thành công. */
    public boolean update(Employee emp) throws SQLException {
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE)) {
            ps.setInt(1, emp.getBranchId());
            ps.setInt(2, emp.getRoleId());          // role_id
            ps.setString(3, emp.getFullName());
            ps.setString(4, emp.getGender());
            ps.setString(5, emp.getBod());
            ps.setString(6, emp.getAddress());
            ps.setString(7, emp.getEmail());
            ps.setString(8, emp.getPhone());
            ps.setString(9, emp.getStatus() != null ? emp.getStatus().name() : "ACTIVE");
            ps.setInt(10, emp.getEmpId());
            return ps.executeUpdate() > 0;
        }
    }

    /** Đổi mật khẩu (đã hash). */
    public boolean updatePassword(int empId, String newPasswordHash) throws SQLException {
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_PASSWORD)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, empId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Xóa nhân viên theo ID. */
    public boolean delete(int empId) throws SQLException {
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE)) {
            ps.setInt(1, empId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Cập nhật trạng thái nhân viên. */
    public boolean updateStatus(int empId, EmployeeStatus status) throws SQLException {
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_STATUS)) {
            ps.setString(1, status.name());
            ps.setInt(2, empId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Lấy danh sách nhân viên theo trạng thái. */
    public List<Employee> getByStatus(EmployeeStatus status) throws SQLException {
        List<Employee> list = new ArrayList<>();
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_STATUS)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Tìm kiếm nhân viên theo tên, email hoặc số điện thoại. */
    public List<Employee> search(String keyword) throws SQLException {
        List<Employee> list = new ArrayList<>();
        String p = "%" + keyword + "%";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(SEARCH)) {
            ps.setString(1, p);
            ps.setString(2, p);
            ps.setString(3, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Đếm số nhân viên của một chi nhánh. */
    public int countByBranch(int branchId) throws SQLException {
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(COUNT_BY_BRANCH)) {
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /** Đếm tổng số nhân viên. */
    public int count() throws SQLException {
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(COUNT_ALL);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}