package dao.branch;

import model.Branch;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BranchDAO {

    // ── Lấy tất cả (kèm số nhân viên) ───────────────────────────
    public List<Branch> findAll() {
        List<Branch> list = new ArrayList<>();
        String sql = """
            SELECT b.*,
                   (SELECT COUNT(*) FROM employee e WHERE e.branch_id = b.branch_id) AS employee_count
            FROM branch b
            ORDER BY b.branch_id
            """;

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── Tìm theo ID (kèm số nhân viên) ──────────────────────────
    public Branch findById(int id) {
        String sql = """
            SELECT b.*,
                   (SELECT COUNT(*) FROM employee e WHERE e.branch_id = b.branch_id) AS employee_count
            FROM branch b
            WHERE b.branch_id = ?
            """;

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ── Thêm mới ────────────────────────────────────────────────
    public int insert(Branch b) {
        String sql = """
            INSERT INTO branch
                (branch_name, branch_code, address, phone, email, opening_time, closing_time, status, city, district, image_url)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setParams(ps, b);
            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
            return -1;

        } catch (SQLIntegrityConstraintViolationException e) {
            // Mã chi nhánh bị trùng (branch_code UNIQUE)
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // ── Cập nhật ────────────────────────────────────────────────
    public boolean update(Branch b) {
        String sql = """
            UPDATE branch
            SET branch_name=?, branch_code=?, address=?, phone=?, email=?,
                opening_time=?, closing_time=?, status=?, city=?, district=?, image_url=?
            WHERE branch_id=?
            """;

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            setParams(ps, b);
            ps.setInt(12, b.getBranchId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Xóa ─────────────────────────────────────────────────────
    public boolean delete(int id) {
        String sql = "DELETE FROM branch WHERE branch_id = ?";

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Kiểm tra branch_code, email, phone trùng (trừ chính nó khi edit) ──────
    public boolean isCodeDuplicate(String code, int excludeId) {
        String sql = "SELECT 1 FROM branch WHERE branch_code = ? AND branch_id != ?";

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean isInforDuplicate(String email,String phone, int excludeId) {
        String sql = "SELECT 1 FROM branch WHERE (email = ? OR phone = ?) AND branch_id != ?";

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            ps.setString(2, phone);
            
            ps.setInt(3, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ── Thống kê dashboard (trang tổng quan cửa hàng) ───────────
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM branch";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Tổng doanh thu đơn hàng đã thanh toán trong ngày hôm nay.
     */
    public double sumTodayRevenue() {
        String sql = """
            SELECT ISNULL(SUM(total_amount), 0)
            FROM [order]
            WHERE CAST(created_at AS DATE) = CAST(GETDATE() AS DATE)
              AND UPPER(status) = 'PAID'
            """;
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Tên chi nhánh có doanh thu cao nhất hôm nay; nếu chưa có đơn thì lấy chi
     * nhánh có nhiều NV nhất.
     */
    public String findBestBranchNameToday() {
        String sqlByRevenue = """
            SELECT TOP 1 b.branch_name
            FROM branch b
            INNER JOIN [order] o ON b.branch_id = o.branch_id
              AND CAST(o.created_at AS DATE) = CAST(GETDATE() AS DATE)
              AND UPPER(o.status) = 'PAID'
            GROUP BY b.branch_id, b.branch_name
            ORDER BY SUM(o.total_amount) DESC
            """;
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlByRevenue); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sqlByEmployees = """
            SELECT TOP 1 b.branch_name
            FROM branch b
            LEFT JOIN employee e ON b.branch_id = e.branch_id
            GROUP BY b.branch_id, b.branch_name
            ORDER BY COUNT(e.emp_id) DESC
            """;
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlByEmployees); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Chưa có dữ liệu";
    }

    /**
     * Doanh thu tháng hiện tại của một chi nhánh (đơn đã thanh toán).
     */
    public double sumMonthlyRevenue(int branchId) {
        String sql = """
            SELECT ISNULL(SUM(total_amount), 0)
            FROM [order]
            WHERE branch_id = ?
              AND MONTH(created_at) = MONTH(GETDATE())
              AND YEAR(created_at) = YEAR(GETDATE())
              AND UPPER(status) = 'PAID'
            """;
        return queryDouble(sql, branchId);
    }

    /**
     * Số đơn hàng tháng hiện tại của một chi nhánh.
     */
    public int countMonthlyOrders(int branchId) {
        String sql = """
            SELECT COUNT(*)
            FROM [order]
            WHERE branch_id = ?
              AND MONTH(created_at) = MONTH(GETDATE())
              AND YEAR(created_at) = YEAR(GETDATE())
              AND UPPER(status) = 'PAID'
            """;
        return queryInt(sql, branchId);
    }

    /**
     * Lợi nhuận ước tính tháng hiện tại (doanh thu sau giảm giá).
     */
    public double sumMonthlyProfit(int branchId) {
        String sql = """
            SELECT ISNULL(SUM(total_amount), 0) - ISNULL(SUM(discount_amount), 0)
            FROM [order]
            WHERE branch_id = ?
              AND MONTH(created_at) = MONTH(GETDATE())
              AND YEAR(created_at) = YEAR(GETDATE())
              AND UPPER(status) = 'PAID'
            """;
        return queryDouble(sql, branchId);
    }

    private double queryDouble(String sql, int branchId) {
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int queryInt(String sql, int branchId) {
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── Lấy danh sách chi nhánh đang active ─────────────────────
    public List<Branch> findAllActive() {
        List<Branch> list = new ArrayList<>();
        String sql = "SELECT * FROM branch WHERE status = 'ACTIVE' ORDER BY branch_id";

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // -- Tìm kiếm thông tin được nhập---------------------------
    public List<Branch> search(String keyword) {

        List<Branch> list = new ArrayList<>();

        String sql = """
        SELECT b.*,
               (SELECT COUNT(*)
                FROM employee e
                WHERE e.branch_id = b.branch_id) AS employee_count
        FROM branch b
        WHERE branch_name LIKE ?
           OR branch_code LIKE ?
        ORDER BY branch_id
        """;

        try (
                Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            String key = "%" + keyword + "%";

            ps.setString(1, key);
            ps.setString(2, key);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    
    //-- Hàm này dùng để đếm có bao nhiêu branch ----------------------------------------------- 
    public int countBranch(String keyword ,String status, String city) {

        StringBuilder sql = new StringBuilder("""
        SELECT COUNT(*)
        FROM branch
        WHERE 1=1
        """);
        
        if  (keyword != null && !keyword.isBlank()){
            sql.append(""" 
                       AND 
                       (
                       branch_name LIKE ?
                       OR 
                       branch_code LIKE ?
                       )
                       """);
        } 
        
        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ?");
        }

        if (city != null && !city.isBlank()) {
            sql.append(" AND city = ?");
        }

        try (
                Connection conn = DBContext.getConnection(); 
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setFilterParams(ps, keyword,status, city);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } 
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return 0;
    }
    
    //-- Hàm này giúp phân trang  -----------------------------------------------
    public List<Branch> findBranchPaging(
            String keyword,
            String status,
            String city,
            int page,
            int pageSize) {

        List<Branch> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
        SELECT b.*,
               (SELECT COUNT(*)
                FROM employee e
                WHERE e.branch_id = b.branch_id) AS employee_count
        FROM branch b
        WHERE 1=1
        """);
        
        if (keyword != null && !keyword.isBlank()){ 
            sql.append("""
                       AND
                       (
                       branch_name LIKE ?
                       OR
                       branch_code Like ?
                       )
                       """);
        } 
        
        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ?");
        }

        if (city != null && !city.isBlank()) {
            sql.append(" AND city = ?");
        }

        sql.append("""
        ORDER BY branch_id
        OFFSET ? ROWS
        FETCH NEXT ? ROWS ONLY
        """);

        try (
                Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = setFilterParams(ps, keyword, status, city);

            ps.setInt(index++, (page - 1) * pageSize);
            ps.setInt(index, pageSize);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // -- Lọc city và status---------------------------------------
    public List<String> getCityWithBranch() {
        List<String> cities = new ArrayList<>();

        String sql = "SELECT DISTINCT city FROM Branch ORDER BY city";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cities.add(rs.getString("city"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return cities;
    }

    // -- Helper: 
    private int setFilterParams(
            PreparedStatement ps,
            String keyword,
            String status,
            String city) throws SQLException {

        int index = 1;
        
        if (keyword != null && !keyword.isBlank()){
            String key = "%" + keyword + "%";
            ps.setString(index++, key);
            ps.setString(index++, key);
        }
        
        if (status != null && !status.isBlank()) {
            ps.setString(index++, status);
        }

        if (city != null && !city.isBlank()) {
            ps.setString(index++, city);
        }

        return index;
    }

    // ── Helper: gán tham số INSERT / UPDATE (8 cột) ─────────────
    private void setParams(PreparedStatement ps, Branch b) throws SQLException {
        ps.setString(1, b.getBranchName());
        ps.setString(2, b.getBranchCode());
        ps.setString(3, b.getAddress());
        ps.setString(4, b.getPhone());
        ps.setString(5, b.getEmail());
        ps.setString(6, b.getOpeningTime());
        ps.setString(7, b.getClosingTime());
        ps.setString(8, b.getStatus());
        ps.setString(9, b.getCity());
        ps.setString(10, b.getDistrict());
        ps.setString(11, b.getImageUrl());
    }

    // ── Helper: ánh xạ ResultSet → Branch ───────────────────────
    private Branch mapRow(ResultSet rs) throws SQLException {
        Branch b = new Branch(
                rs.getInt("branch_id"),
                rs.getString("branch_name"),
                rs.getString("branch_code"),
                rs.getString("address"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("opening_time"),
                rs.getString("closing_time"),
                rs.getString("status"),
                rs.getString("created_at"),
                rs.getString("update_at"),
                rs.getString("city"),
                rs.getString("district"),
                rs.getString("image_url")
        );
        try {
            b.setEmployeeCount(rs.getInt("employee_count"));
        } catch (SQLException ignored) {
            // Truy vấn không có cột employee_count
        }
        return b;
    }
}
