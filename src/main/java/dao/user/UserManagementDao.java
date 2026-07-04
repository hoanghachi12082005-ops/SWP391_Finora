/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao.user;

/**
 *
 * @author Dzung
 */
import java.math.BigDecimal;
import model.Branch;
import model.Employee;
import model.Role;
import util.database.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.EmployeeOverview;

public class UserManagementDao extends DBContext {

    // =====================================================
    // COMMON SELECT PART
    // =====================================================

    private static final String USER_SELECT =
            "SELECT DISTINCT " +
            "    e.emp_id AS EmployeeID, " +
            "    e.role_id AS RoleID, " +
            "    e.branch_id AS BranchID, " +
            "    e.fullName AS FullName, " +
            "    e.email AS Email, " +
            "    e.phone AS Phone, " +
            "    e.status AS Status, " +
            "    e.created_at AS CreatedAt, " +
            "    e.image_URL AS AvatarUrl, " +
            "    r.role_name AS RoleName, " +
            "    b.branch_name AS BranchName, " +
            "    r.role_name AS RoleNames " +
            "FROM Employee e " +
            "LEFT JOIN Role r ON e.role_id = r.role_id " +
            "LEFT JOIN Branch b ON e.branch_id = b.branch_id ";

    // =====================================================
    // ADMIN SIDE
    // Admin view/add/edit/lock/unlock/reset Owner
    // =====================================================




//

//

//

//


    // =====================================================
    // ADMIN: Owner Management
    // =====================================================

    public List<Employee> getOwners(String keyword, String statusFilter) {
        List<Employee> list = new ArrayList<>();
        String sql =
                USER_SELECT +
                "WHERE r.role_name = 'Owner' " +
                "AND (? IS NULL OR e.fullName LIKE ? OR e.email LIKE ? OR e.phone LIKE ?) " +
                "AND (? IS NULL OR e.status = ?) " +
                "ORDER BY e.emp_id DESC";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            String searchValue = normalizeLike(keyword);
            String statusValue = normalize(statusFilter);

            ps.setString(1, searchValue);
            ps.setString(2, searchValue);
            ps.setString(3, searchValue);
            ps.setString(4, searchValue);
            ps.setString(5, statusValue);
            ps.setString(6, statusValue);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEmployee(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Employee getOwnerById(int employeeId) {
        String sql = USER_SELECT + "WHERE e.emp_id = ? AND r.role_name = 'Owner'";
        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapEmployee(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateOwner(Employee employee) {
        String sql =
                "UPDATE Employee SET role_id = ?, branch_id = ?, fullName = ?, email = ?, phone = ?, status = ?, update_at = GETDATE() " +
                "WHERE emp_id = ? " +
                "AND EXISTS (SELECT 1 FROM Role rr WHERE rr.role_id = Employee.role_id AND rr.role_name = 'Owner')";

        try (Connection connection = DBContext.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, employee.getRoleID());
                setNullableInt(ps, 2, employee.getBranchID());
                ps.setString(3, employee.getFullName());
                ps.setString(4, employee.getEmail());
                ps.setString(5, employee.getPhone());
                ps.setString(6, employee.getStatus());
                ps.setInt(7, employee.getEmployeeID());
                int updated = ps.executeUpdate();
                if (updated == 0) { connection.rollback(); return false; }
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateOwnerStatus(int employeeId, String status) {
        String sql =
                "UPDATE Employee SET status = ?, update_at = GETDATE() WHERE emp_id = ? " +
                "AND EXISTS (SELECT 1 FROM Role rr WHERE rr.role_id = Employee.role_id AND rr.role_name = 'Owner')";
        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean resetOwnerPassword(int employeeId, String hashedPassword) {
        String sql =
                "UPDATE Employee SET passwordHash = ?, update_at = GETDATE() WHERE emp_id = ? " +
                "AND EXISTS (SELECT 1 FROM Role rr WHERE rr.role_id = Employee.role_id AND rr.role_name = 'Owner')";
        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // =====================================================
    // ADMIN: any role (no Owner/Admin exclusion)
    // =====================================================

    public boolean updateEmployeeByAdmin(Employee employee) {
        String sql =
                "UPDATE Employee SET role_id = ?, branch_id = ?, fullName = ?, email = ?, phone = ?, status = ?, update_at = GETDATE() " +
                "WHERE emp_id = ?";
        try (Connection connection = DBContext.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, employee.getRoleID());
                setNullableInt(ps, 2, employee.getBranchID());
                ps.setString(3, employee.getFullName());
                ps.setString(4, employee.getEmail());
                ps.setString(5, employee.getPhone());
                ps.setString(6, employee.getStatus());
                ps.setInt(7, employee.getEmployeeID());
                int updated = ps.executeUpdate();
                if (updated == 0) { connection.rollback(); return false; }
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateEmployeeStatusByAdmin(int employeeId, String status) {
        String sql = "UPDATE Employee SET status = ?, update_at = GETDATE() WHERE emp_id = ?";
        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean resetPasswordByAdmin(int employeeId, String hashedPassword) {
        String sql = "UPDATE Employee SET passwordHash = ?, update_at = GETDATE() WHERE emp_id = ?";
        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // =====================================================
    // ALL EMPLOYEES (Admin view - includes all roles)
    // =====================================================

    public List<Employee> getAllEmployees(String keyword,
                                          String branchFilter,
                                          String roleFilter,
                                          String statusFilter,
                                          int page,
                                          int pageSize) {
        List<Employee> list = new ArrayList<>();
        String sql =
                USER_SELECT +
                "WHERE (? IS NULL OR e.fullName LIKE ? OR e.email LIKE ? OR e.phone LIKE ?) " +
                "AND (? IS NULL OR e.branch_id = ?) " +
                "AND (? IS NULL OR e.role_id = ?) " +
                "AND (? IS NULL OR e.status = ?) " +
                "ORDER BY e.emp_id DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            String searchValue = normalizeLike(keyword);
            Integer branchId = parseInteger(branchFilter);
            Integer roleId = parseInteger(roleFilter);
            String statusValue = normalize(statusFilter);
            int offset = (page - 1) * pageSize;

            ps.setString(1, searchValue);
            ps.setString(2, searchValue);
            ps.setString(3, searchValue);
            ps.setString(4, searchValue);
            setNullableInt(ps, 5, branchId);
            setNullableInt(ps, 6, branchId);
            setNullableInt(ps, 7, roleId);
            setNullableInt(ps, 8, roleId);
            ps.setString(9, statusValue);
            ps.setString(10, statusValue);
            ps.setInt(11, offset);
            ps.setInt(12, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapEmployee(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countAllEmployees(String keyword,
                                 String branchFilter,
                                 String roleFilter,
                                 String statusFilter) {
        String sql =
                "SELECT COUNT(DISTINCT e.emp_id) AS Total " +
                "FROM Employee e " +
                "LEFT JOIN Role r ON e.role_id = r.role_id " +
                "WHERE (? IS NULL OR e.fullName LIKE ? OR e.email LIKE ? OR e.phone LIKE ?) " +
                "AND (? IS NULL OR e.branch_id = ?) " +
                "AND (? IS NULL OR e.role_id = ?) " +
                "AND (? IS NULL OR e.status = ?)";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            String searchValue = normalizeLike(keyword);
            Integer branchId = parseInteger(branchFilter);
            Integer roleId = parseInteger(roleFilter);
            String statusValue = normalize(statusFilter);

            ps.setString(1, searchValue);
            ps.setString(2, searchValue);
            ps.setString(3, searchValue);
            ps.setString(4, searchValue);
            setNullableInt(ps, 5, branchId);
            setNullableInt(ps, 6, branchId);
            setNullableInt(ps, 7, roleId);
            setNullableInt(ps, 8, roleId);
            ps.setString(9, statusValue);
            ps.setString(10, statusValue);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("Total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public EmployeeOverview getAllEmployeesOverview() {
        EmployeeOverview overview = new EmployeeOverview();
        String sql =
                "SELECT " +
                "    COUNT(DISTINCT e.emp_id) AS TotalEmployees, " +
                "    COUNT(DISTINCT CASE WHEN e.status = 'active' THEN e.emp_id END) AS ActiveEmployees, " +
                "    COUNT(o.order_id) AS TotalOrders, " +
                "    COALESCE(SUM(o.total_amount), 0) AS TotalRevenue " +
                "FROM Employee e " +
                "LEFT JOIN [order] o ON e.emp_id = o.emp_id " +
                "LEFT JOIN Role r ON e.role_id = r.role_id " +
                "WHERE (r.role_name IS NULL OR r.role_name NOT IN ('Admin'))";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                overview.setTotalEmployees(rs.getInt("TotalEmployees"));
                overview.setActiveEmployees(rs.getInt("ActiveEmployees"));
                overview.setTotalOrders(rs.getInt("TotalOrders"));
                BigDecimal revenue = rs.getBigDecimal("TotalRevenue");
                overview.setTotalRevenue(revenue == null ? BigDecimal.ZERO : revenue);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return overview;
    }

    // =====================================================
    // OWNER SIDE
    // Owner view/add/edit/lock/unlock/reset Employee
    // =====================================================

    public List<Employee> getEmployees(String keyword,
                                   String branchFilter,
                                   String roleFilter,
                                   String statusFilter,
                                   int page,
                                   int pageSize) {
        List<Employee> list = new ArrayList<Employee>();

        String sql =
                USER_SELECT +
                "WHERE (r.role_name IS NULL OR r.role_name NOT IN ('Admin', 'Owner')) " +
                "AND ( " +
                "    ? IS NULL " +
                "    OR e.fullName LIKE ? " +
                "    OR e.email LIKE ? " +
                "    OR e.phone LIKE ? " +
                ") " +
                "AND (? IS NULL OR e.branch_id = ?) " +
                "AND (? IS NULL OR e.role_id = ?) " +
                "AND (? IS NULL OR e.status = ?) " +
                "ORDER BY e.emp_id DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql);) {
            String searchValue = normalizeLike(keyword);
            Integer branchId = parseInteger(branchFilter);
            Integer roleId = parseInteger(roleFilter);
            String statusValue = normalize(statusFilter);

            int offset = (page - 1) * pageSize;

            ps.setString(1, searchValue);
            ps.setString(2, searchValue);
            ps.setString(3, searchValue);
            ps.setString(4, searchValue);

            setNullableInt(ps, 5, branchId);
            setNullableInt(ps, 6, branchId);

            setNullableInt(ps, 7, roleId);
            setNullableInt(ps, 8, roleId);

            ps.setString(9, statusValue);
            ps.setString(10, statusValue);

            ps.setInt(11, offset);
            ps.setInt(12, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEmployee(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    public int countEmployees(String keyword,
                          String branchFilter,
                          String roleFilter,
                          String statusFilter) {
        String sql =
                "SELECT COUNT(DISTINCT e.emp_id) AS Total " +
                "FROM Employee e " +
                "LEFT JOIN Role r ON e.role_id = r.role_id " +
                "WHERE (r.role_name IS NULL OR r.role_name NOT IN ('Admin', 'Owner')) " +
                "AND ( " +
                "    ? IS NULL " +
                "    OR e.fullName LIKE ? " +
                "    OR e.email LIKE ? " +
                "    OR e.phone LIKE ? " +
                ") " +
                "AND (? IS NULL OR e.branch_id = ?) " +
                "AND (? IS NULL OR e.role_id = ?) " +
                "AND (? IS NULL OR e.status = ?)";

        try (Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql);) {
            String searchValue = normalizeLike(keyword);
            Integer branchId = parseInteger(branchFilter);
            Integer roleId = parseInteger(roleFilter);
            String statusValue = normalize(statusFilter);

            ps.setString(1, searchValue);
            ps.setString(2, searchValue);
            ps.setString(3, searchValue);
            ps.setString(4, searchValue);

            setNullableInt(ps, 5, branchId);
            setNullableInt(ps, 6, branchId);

            setNullableInt(ps, 7, roleId);
            setNullableInt(ps, 8, roleId);

            ps.setString(9, statusValue);
            ps.setString(10, statusValue);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
    public List<Employee> getEmployeesByBranch(int managerBranchId,
                                           String keyword,
                                           String roleFilter,
                                           String statusFilter,
                                           int page,
                                           int pageSize) {
        List<Employee> list = new ArrayList<Employee>();

        String sql =
                USER_SELECT +
                "WHERE e.branch_id = ? " +
                "AND (r.role_name IS NULL OR r.role_name NOT IN ('Admin', 'Owner')) " +
                "AND ( " +
                "    ? IS NULL " +
                "    OR e.fullName LIKE ? " +
                "    OR e.email LIKE ? " +
                "    OR e.phone LIKE ? " +
                ") " +
                "AND (? IS NULL OR e.role_id = ?) " +
                "AND (? IS NULL OR e.status = ?) " +
                "ORDER BY e.emp_id DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql);) {
            String searchValue = normalizeLike(keyword);
            Integer roleId = parseInteger(roleFilter);
            String statusValue = normalize(statusFilter);

            int offset = (page - 1) * pageSize;

            ps.setInt(1, managerBranchId);

            ps.setString(2, searchValue);
            ps.setString(3, searchValue);
            ps.setString(4, searchValue);
            ps.setString(5, searchValue);

            setNullableInt(ps, 6, roleId);
            setNullableInt(ps, 7, roleId);

            ps.setString(8, statusValue);
            ps.setString(9, statusValue);

            ps.setInt(10, offset);
            ps.setInt(11, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEmployee(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    public int countEmployeesByBranch(int managerBranchId,
                                  String keyword,
                                  String roleFilter,
                                  String statusFilter) {
        String sql =
                "SELECT COUNT(DISTINCT e.emp_id) AS Total " +
                "FROM Employee e " +
                "LEFT JOIN Role r ON e.role_id = r.role_id " +
                "WHERE e.branch_id = ? " +
                "AND (r.role_name IS NULL OR r.role_name NOT IN ('Admin', 'Owner')) " +
                "AND ( " +
                "    ? IS NULL " +
                "    OR e.fullName LIKE ? " +
                "    OR e.email LIKE ? " +
                "    OR e.phone LIKE ? " +
                ") " +
                "AND (? IS NULL OR e.role_id = ?) " +
                "AND (? IS NULL OR e.status = ?)";

        try (Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql);) {
            String searchValue = normalizeLike(keyword);
            Integer roleId = parseInteger(roleFilter);
            String statusValue = normalize(statusFilter);

            ps.setInt(1, managerBranchId);

            ps.setString(2, searchValue);
            ps.setString(3, searchValue);
            ps.setString(4, searchValue);
            ps.setString(5, searchValue);

            setNullableInt(ps, 6, roleId);
            setNullableInt(ps, 7, roleId);

            ps.setString(8, statusValue);
            ps.setString(9, statusValue);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
    public Employee getEmployeeById(int employeeId) {
        String sql =
                USER_SELECT +
                "WHERE e.emp_id = ? " +
                "AND (r.role_name IS NULL OR r.role_name NOT IN ('Admin', 'Owner'))";

        try (Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql);) {
            ps.setInt(1, employeeId);

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

    

    public boolean addEmployee(Employee employee, String hashedPassword) {
        int primaryRoleId = employee.getRoleID();

        String insertEmployeeSql =
                "INSERT INTO Employee " +
                "(role_id, branch_id, fullName, email, phone, passwordHash, status, created_at, update_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";

        try (Connection connection = DBContext.getConnection()) {
            connection.setAutoCommit(false);

            int newEmployeeId;

            try (PreparedStatement ps = connection.prepareStatement(
                    insertEmployeeSql,
                    Statement.RETURN_GENERATED_KEYS
            )) {
                ps.setInt(1, primaryRoleId);
                setNullableInt(ps, 2, employee.getBranchID());
                ps.setString(3, employee.getFullName());
                ps.setString(4, employee.getEmail());
                ps.setString(5, employee.getPhone());
                ps.setString(6, hashedPassword);
                String rawStatus = normalize(employee.getStatus());
                ps.setString(7, rawStatus == null ? "ACTIVE" : rawStatus.toUpperCase());

                int affectedRows = ps.executeUpdate();

                if (affectedRows == 0) {
                    connection.rollback();
                    return false;
                }

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        connection.rollback();
                        return false;
                    }

                    newEmployeeId = keys.getInt(1);
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateEmployee(Employee employee) {
        int primaryRoleId = employee.getRoleID();

        String updateEmployeeSql =
                "UPDATE Employee " +
                "SET role_id = ?, " +
                "    branch_id = ?, " +
                "    fullName = ?, " +
                "    email = ?, " +
                "    phone = ?, " +
                "    status = ?, " +
                "    update_at = GETDATE(), " +
                "    failed_login_count = CASE WHEN UPPER(?) = 'ACTIVE' THEN 0 ELSE failed_login_count END " +
                "WHERE emp_id = ? " +
                "AND NOT EXISTS (SELECT 1 FROM Role rr WHERE rr.role_id = Employee.role_id AND rr.role_name IN ('Admin', 'Owner'))";

        try (Connection connection = DBContext.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(updateEmployeeSql)) {
                ps.setInt(1, primaryRoleId);
                setNullableInt(ps, 2, employee.getBranchID());
                ps.setString(3, employee.getFullName());
                ps.setString(4, employee.getEmail());
                ps.setString(5, employee.getPhone());
                ps.setString(6, employee.getStatus());
                ps.setString(7, employee.getStatus());
                ps.setInt(8, employee.getEmployeeID());

                int updated = ps.executeUpdate();

                if (updated == 0) {
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateEmployeeStatus(int employeeId, String status) {
        String sql =
                "UPDATE Employee " +
                "SET status = ?, " +
                "    update_at = GETDATE(), " +
                "    failed_login_count = CASE WHEN UPPER(?) = 'ACTIVE' THEN 0 ELSE failed_login_count END " +
                "WHERE emp_id = ? " +
                "AND NOT EXISTS (SELECT 1 FROM Role rr WHERE rr.role_id = Employee.role_id AND rr.role_name IN ('Admin', 'Owner'))";

        try (Connection connection = DBContext.getConnection(); 
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, status);
            ps.setInt(3, employeeId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean resetEmployeePassword(int employeeId, String hashedPassword) {
        String sql =
                "UPDATE Employee " +
                "SET passwordHash = ?, update_at = GETDATE() " +
                "WHERE emp_id = ? " +
                "AND NOT EXISTS (SELECT 1 FROM Role rr WHERE rr.role_id = Employee.role_id AND rr.role_name IN ('Admin', 'Owner'))";

        try (Connection connection = DBContext.getConnection(); 
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, employeeId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // =====================================================
    // MANAGER SIDE
    // StoreManager view employees in assigned branch
    // =====================================================

    public List<Employee> getEmployeesByBranch(int managerBranchId,
                                               String keyword,
                                               String roleFilter,
                                               String statusFilter) {
        List<Employee> list = new ArrayList<Employee>();

        String sql =
                USER_SELECT +
                "WHERE e.branch_id = ? " +
                "AND (r.role_name IS NULL OR r.role_name NOT IN ('Admin', 'Owner')) " +
                "AND ( " +
                "    ? IS NULL " +
                "    OR e.fullName LIKE ? " +
                "    OR e.email LIKE ? " +
                "    OR e.phone LIKE ? " +
                ") " +
                "AND (? IS NULL OR e.role_id = ?) " +
                "AND (? IS NULL OR e.status = ?) " +
                "ORDER BY e.emp_id DESC";

        try (Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql);) {
            String searchValue = normalizeLike(keyword);
            Integer roleId = parseInteger(roleFilter);
            String statusValue = normalize(statusFilter);

            ps.setInt(1, managerBranchId);

            ps.setString(2, searchValue);
            ps.setString(3, searchValue);
            ps.setString(4, searchValue);
            ps.setString(5, searchValue);

            setNullableInt(ps, 6, roleId);
            setNullableInt(ps, 7, roleId);

            ps.setString(8, statusValue);
            ps.setString(9, statusValue);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEmployee(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Employee getEmployeeByIdInBranch(int employeeId, int managerBranchId) {
        String sql =
                USER_SELECT +
                "WHERE e.emp_id = ? " +
                "AND e.branch_id = ? " +
                "AND NOT EXISTS (SELECT 1 FROM Role rr WHERE rr.role_id = Employee.role_id AND rr.role_name IN ('Admin', 'Owner'))";

        try (Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, managerBranchId);

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
    public EmployeeOverview getOwnerEmployeeOverview() {
        EmployeeOverview overview = new EmployeeOverview();

        loadTotalInfo(overview, null);
        loadTopEmployee(overview, null);

        return overview;
    }

    public EmployeeOverview getManagerEmployeeOverview(int branchId) {
        EmployeeOverview overview = new EmployeeOverview();

        loadTotalInfo(overview, branchId);
        loadTopEmployee(overview, branchId);

        return overview;
    }

    private void loadTotalInfo(EmployeeOverview overview, Integer branchId) {
        String sql =
                "SELECT " +
                "    COUNT(DISTINCT e.emp_id) AS TotalEmployees, " +
                "    COUNT(DISTINCT CASE WHEN e.status = 'active' THEN e.emp_id END) AS ActiveEmployees, " +
                "    COUNT(o.order_id) AS TotalOrders, " +
                "    COALESCE(SUM(o.total_amount), 0) AS TotalRevenue " +
                "FROM Employee e " +
                "LEFT JOIN [order] o ON e.emp_id = o.emp_id " +
                "WHERE NOT EXISTS ( " +
                "    SELECT 1 " +
                "    FROM Role r " +
                "    WHERE r.role_id = e.role_id " +
                "      AND r.role_name IN ('Admin', 'Owner') " +
                ") " +
                "AND (? IS NULL OR e.branch_id = ?)";

        try (Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql)) {
            if (branchId == null) {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(1, branchId);
                ps.setInt(2, branchId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    overview.setTotalEmployees(rs.getInt("TotalEmployees"));
                    overview.setActiveEmployees(rs.getInt("ActiveEmployees"));
                    overview.setTotalOrders(rs.getInt("TotalOrders"));

                    BigDecimal revenue = rs.getBigDecimal("TotalRevenue");
                    overview.setTotalRevenue(revenue == null ? BigDecimal.ZERO : revenue);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTopEmployee(EmployeeOverview overview, Integer branchId) {
        String sql =
                "SELECT TOP 1 " +
                "    e.fullName, " +
                "    COALESCE(SUM(o.total_amount), 0) AS EmployeeRevenue " +
                "FROM Employee e " +
                "LEFT JOIN [order] o ON e.emp_id = o.emp_id " +
                "WHERE NOT EXISTS ( " +
                "    SELECT 1 " +
                "    FROM Role r " +
                "    WHERE r.role_id = e.role_id " +
                "      AND r.role_name IN ('Admin', 'Owner') " +
                ") " +
                "AND (? IS NULL OR e.branch_id = ?) " +
                "GROUP BY e.emp_id, e.fullName " +
                "ORDER BY EmployeeRevenue DESC";

        try (Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql)) {
            if (branchId == null) {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(1, branchId);
                ps.setInt(2, branchId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    overview.setTopEmployeeName(rs.getString("fullName"));

                    BigDecimal revenue = rs.getBigDecimal("EmployeeRevenue");
                    overview.setTopEmployeeRevenue(revenue == null ? BigDecimal.ZERO : revenue);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =====================================================
    // COMMON DATA FOR USER MANAGEMENT PAGE
    // =====================================================

    public List<Role> getAllRoles() {
        List<Role> list = new ArrayList<>();
        String sql = "SELECT role_id AS RoleID, role_name AS Name, discription AS Description FROM Role ORDER BY role_id";
        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Role role = new Role();
                role.setRoleID(rs.getInt("RoleID"));
                role.setName(rs.getString("Name"));
                role.setDescription(rs.getString("Description"));
                list.add(role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Employee getEmployeeByIdAllRoles(int id) {
        String sql = USER_SELECT + "WHERE e.emp_id = ?";
        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapEmployee(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Role> getEmployeeRoles() {
        List<Role> list = new ArrayList<Role>();

        String sql =
                "SELECT role_id AS RoleID, role_name AS Name, discription AS Description " +
                "FROM Role " +
                "WHERE role_name IN ('StoreManager', 'SalesStaff', 'WarehouseStaff') " +
                "ORDER BY role_id";

        try (Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Role role = new Role();

                role.setRoleID(rs.getInt("RoleID"));
                role.setName(rs.getString("Name"));
                role.setDescription(rs.getString("Description"));

                list.add(role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Branch> getAllBranches() {
        List<Branch> list = new ArrayList<Branch>();

        String sql =
                "SELECT branch_id AS BranchID, branch_name AS Name, address AS Address, phone AS Phone, status AS Status " +
                "FROM Branch " +
                "ORDER BY branch_id";

        try (Connection connection = DBContext.getConnection(); 
             PreparedStatement ps = connection.prepareStatement(sql);
              ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Branch branch = new Branch();

                branch.setBranchID(rs.getInt("BranchID"));
                branch.setName(rs.getString("Name"));
                branch.setAddress(rs.getString("Address"));
                branch.setPhone(rs.getString("Phone"));
                branch.setStatus(rs.getString("Status"));

                list.add(branch);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // =====================================================
    // DUPLICATE CHECK
    // =====================================================

    public boolean isEmailExists(String email, Integer excludeEmployeeId) {
        return isEmailExists(email, null, excludeEmployeeId);
    }

    public boolean isEmailExists(String email, String phone, Integer excludeEmployeeId) {
        String normalizedEmail = normalize(email);
        String normalizedPhone = normalize(phone);

        if (normalizedEmail == null && normalizedPhone == null) {
            return false;
        }
            
        String sql =
                "SELECT COUNT(*) AS Total " +
                "FROM Employee " +
                "WHERE ( " +
                "    (? IS NOT NULL AND email = ?) " +
                "    OR (? IS NOT NULL AND phone = ?) " +
                ") " +
                "AND (? IS NULL OR emp_id <> ?)";
        
        try (Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, normalizedEmail);
            ps.setString(2, normalizedEmail);
            ps.setString(3, normalizedPhone);
            ps.setString(4, normalizedPhone);

            if (excludeEmployeeId == null) {
                ps.setNull(5, Types.INTEGER);
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(5, excludeEmployeeId);
                ps.setInt(6, excludeEmployeeId);
            }

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

    // =====================================================
    // PRIVATE HELPER METHODS
    // =====================================================
    
    public Employee getEmployeeInfoById(int id) {
        String sql = "SELECT emp_id AS EmployeeID, email AS Email, fullName AS FullName " +
                     "FROM Employee " +
                     "WHERE emp_id = ?";

        try (Connection connection = DBContext.getConnection(); 
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Employee employee = new Employee();

                    employee.setEmployeeID(rs.getInt("EmployeeID"));
                    employee.setEmail(rs.getString("Email"));
                    employee.setFullName(rs.getString("FullName"));

                    return employee;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    private int getRoleIdByName(String roleName) {
        String sql =
                "SELECT role_id AS RoleID " +
                "FROM Role " +
                "WHERE role_name = ?";

        try (Connection connection = DBContext.getConnection(); 
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, roleName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("RoleID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    private Employee mapEmployee(ResultSet rs) throws SQLException {
        Employee employee = new Employee();

        employee.setEmployeeID(rs.getInt("EmployeeID"));
        employee.setRoleID(rs.getInt("RoleID"));

        int branchId = rs.getInt("BranchID");
        if (rs.wasNull()) {
            employee.setBranchID((Integer) null);
        } else {
            employee.setBranchID(branchId);
        }

        employee.setFullName(rs.getString("FullName"));
        employee.setEmail(rs.getString("Email"));
        employee.setPhone(rs.getString("Phone"));
        employee.setStatus(rs.getString("Status"));
        employee.setCreatedAt(rs.getTimestamp("CreatedAt"));
        employee.setAvatarUrl(rs.getString("AvatarUrl"));

        employee.setRoleName(rs.getString("RoleName"));
        employee.setRoleNames(rs.getString("RoleNames"));
        employee.setBranchName(rs.getString("BranchName"));

        return employee;
    }

    private String normalize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String normalizeLike(String value) {
        String normalized = normalize(value);

        if (normalized == null) {
            return null;
        }

        normalized = normalized.replaceAll("\\s+", " ");

        return "%" + normalized + "%";
    }

    private Integer parseInteger(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }

            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value)
            throws SQLException {
        if (value == null || value <= 0) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }


}
