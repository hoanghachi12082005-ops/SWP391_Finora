package dao.supplier;

import model.Supplier;
import util.database.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    /**
     * Đếm tổng số nhà cung cấp
     */
    public int countSuppliers(String keyword, String status) {
        if (keyword == null) {
            keyword = "";
        }
        if (status == null) {
            status = "";
        }

        keyword = keyword.trim().replaceAll("\\s+", " ");
        String search = "%" + keyword + "%";

        String sql = """
        SELECT COUNT(*)
        FROM supplier
        WHERE
        (
            ? = ''
            OR supplier_name LIKE ?
            OR phone_number LIKE ?
            OR address LIKE ?
        )
        AND
        (
            ? = ''
            OR status = ?
        )
        """;

        try (Connection conn = DBContext.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, keyword);
            ps.setString(2, search);
            ps.setString(3, search);
            ps.setString(4, search);
            ps.setString(5, status);
            ps.setString(6, status);

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

    /**
     * Lấy danh sách supplier có phân trang
     */
    public List<Supplier> getSuppliersPaging(
            String keyword,
            String status,
            int page,
            int pageSize) {

        List<Supplier> list = new ArrayList<>();

        if (keyword == null) {
            keyword = "";
        }
        if (status == null) {
            status = "";
        }

        keyword = keyword.trim().replaceAll("\\s+", " ");
        String search = "%" + keyword + "%";

        String sql = """
        SELECT 
            supplier_id AS SupplierID,
            supplier_name AS Name,
            phone_number AS Phone,
            address AS Address,
            status AS Status,
            created_at AS CreatedAt,
            updated_at AS UpdatedAt
        FROM supplier
        WHERE
        (
            ? = ''
            OR supplier_name LIKE ?
            OR phone_number LIKE ?
            OR address LIKE ?
        )
        AND
        (
            ? = ''
            OR status = ?
        )
        ORDER BY supplier_id ASC 
        OFFSET ? ROWS
        FETCH NEXT ? ROWS ONLY
        """;

        try (Connection conn = DBContext.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, keyword);
            ps.setString(2, search);
            ps.setString(3, search);
            ps.setString(4, search);
            ps.setString(5, status);
            ps.setString(6, status);
            ps.setInt(7, (page - 1) * pageSize);
            ps.setInt(8, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Supplier s = new Supplier();
                    s.setSupplierID(rs.getInt("SupplierID"));
                    s.setName(rs.getString("Name"));
                    s.setPhone(rs.getString("Phone"));
                    s.setAddress(rs.getString("Address"));
                    s.setStatus(rs.getString("Status"));
                    s.setCreatedAt(rs.getTimestamp("CreatedAt"));
                    s.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
                    list.add(s);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Lấy supplier theo ID
     */
    public Supplier getById(int supplierId) {
        String sql = """
        SELECT 
            supplier_id AS SupplierID,
            supplier_name AS Name,
            phone_number AS Phone,
            address AS Address,
            status AS Status,
            created_at AS CreatedAt,
            updated_at AS UpdatedAt
        FROM supplier
        WHERE supplier_id = ?
        """;

        try (Connection conn = DBContext.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, supplierId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Supplier s = new Supplier();
                    s.setSupplierID(rs.getInt("SupplierID"));
                    s.setName(rs.getString("Name"));
                    s.setPhone(rs.getString("Phone"));
                    s.setAddress(rs.getString("Address"));
                    s.setStatus(rs.getString("Status"));
                    s.setCreatedAt(rs.getTimestamp("CreatedAt"));
                    s.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
                    return s;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Kiểm tra nhà cung cấp tồn tại theo tên hoặc số điện thoại
     */
    public boolean existsByNameOrPhone(String name, String phone) {
        String sql = """
        SELECT COUNT(*)
        FROM supplier
        WHERE LOWER(supplier_name) = LOWER(?) OR phone_number = ?
        """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, phone.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Thêm hoặc cập nhật supplier
     */
    public boolean save(Supplier supplier) {
        if (supplier.getSupplierID() <= 0) {
            String sql = """
            INSERT INTO supplier
            (
                supplier_name,
                phone_number,
                address,
                status,
                created_at,
                updated_at
            )
            VALUES
            (
                ?, ?, ?, ?,
                GETDATE(),
                GETDATE()
            )
            """;

            try (Connection conn = DBContext.getConnection(); 
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, supplier.getName());
                ps.setString(2, supplier.getPhone());
                ps.setString(3, supplier.getAddress());
                ps.setString(4, supplier.getStatus());

                return ps.executeUpdate() > 0;

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String sql = """
            UPDATE supplier
            SET
                supplier_name = ?,
                phone_number = ?,
                address = ?,
                status = ?,
                updated_at = GETDATE()
            WHERE supplier_id = ?
            """;

            try (Connection conn = DBContext.getConnection(); 
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, supplier.getName());
                ps.setString(2, supplier.getPhone());
                ps.setString(3, supplier.getAddress());
                ps.setString(4, supplier.getStatus());
                ps.setInt(5, supplier.getSupplierID());

                return ps.executeUpdate() > 0;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Xóa supplier
     */
    public boolean delete(int supplierId) {
        String sql = """
        DELETE FROM supplier
        WHERE supplier_id = ?
        """;

        try (Connection conn = DBContext.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, supplierId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Đếm supplier Active
     */
    public int countActiveSuppliers() {
        String sql = """
        SELECT COUNT(*)
        FROM supplier
        WHERE status = 'active'
        """;

        try (Connection conn = DBContext.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql); 
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Đếm supplier Inactive
     */
    public int countInactiveSuppliers() {
        String sql = """
        SELECT COUNT(*)
        FROM supplier
        WHERE status = 'inactive'
        """;

        try (Connection conn = DBContext.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql); 
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Lấy danh sách sản phẩm và giá nhập gần đây nhất của nhà cung cấp dựa trên lịch sử order_detail.
     */
    public List<dto.inventory.ImportProductDTO.SupplierInfo> getSupplierProductsHistory(int supplierId) {
        List<dto.inventory.ImportProductDTO.SupplierInfo> list = new ArrayList<>();
        String sql = """
            WITH LatestPrices AS (
                SELECT 
                    od.product_id,
                    p.product_name,
                    od.import_price,
                    ROW_NUMBER() OVER (PARTITION BY od.product_id ORDER BY o.created_at DESC) as rn
                FROM order_detail od
                JOIN [order] o ON od.order_id = o.order_id
                JOIN product p ON od.product_id = p.product_id
                WHERE o.order_type = 'PURCHASE' AND o.supplier_id = ?
            )
            SELECT product_id, product_name, import_price 
            FROM LatestPrices 
            WHERE rn = 1
            ORDER BY product_name ASC
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new dto.inventory.ImportProductDTO.SupplierInfo(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getBigDecimal("import_price")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteSupplierProduct(int supplierId, int productId) {
        String sql = """
            DELETE od 
            FROM order_detail od
            JOIN [order] o ON od.order_id = o.order_id
            WHERE o.order_type = 'PURCHASE' AND o.supplier_id = ? AND od.product_id = ?
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            ps.setInt(2, productId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addOrUpdateSupplierProduct(int supplierId, int productId, double importPrice) {
        String insertOrderSql = """
            INSERT INTO [order] (order_code, order_type, supplier_id, subtotal, discount_amount, total_amount, payment_method, status, created_at)
            VALUES (?, 'PURCHASE', ?, 0, 0, 0, 'BANK_TRANSFER', 'COMPLETED', GETDATE())
            """;
        String insertDetailSql = """
            INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price, import_price)
            VALUES (?, ?, 0, 0, 0, ?)
            """;
        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);
            
            int orderId = 0;
            String orderCode = "PO-ADJ-" + System.currentTimeMillis();
            try (PreparedStatement ps = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, orderCode);
                ps.setInt(2, supplierId);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                    }
                }
            }
            
            if (orderId > 0) {
                try (PreparedStatement ps = conn.prepareStatement(insertDetailSql)) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, productId);
                    ps.setDouble(3, importPrice);
                    ps.executeUpdate();
                }
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
        return false;
    }
}
