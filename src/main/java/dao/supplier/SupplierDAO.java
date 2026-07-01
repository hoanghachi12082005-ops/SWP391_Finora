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
}
