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
        FROM Supplier
        WHERE
        (
            ? = ''
            OR Name LIKE ?
            OR Phone LIKE ?
            OR Address LIKE ?
        )
        AND
        (
            ? = ''
            OR Status = ?
        )
        """;

        try (
                Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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
    SELECT *
    FROM Supplier
    WHERE
    (
        ? = ''
        OR Name LIKE ?
        OR Phone LIKE ?
        OR Address LIKE ?
    )
    AND
    (
        ? = ''
        OR Status = ?
    )
    ORDER BY SupplierID DESC
    OFFSET ? ROWS
    FETCH NEXT ? ROWS ONLY
    """;

        try (
                Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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
                SELECT *
                FROM Supplier
                WHERE SupplierID = ?
                """;

        try (
                Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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
     * Thêm supplier
     */
    public boolean insert(Supplier supplier) {

        String sql = """
                INSERT INTO Supplier
                (
                    Name,
                    Phone,
                    Address,
                    Status,
                    CreatedAt,
                    UpdatedAt
                )
                VALUES
                (
                    ?, ?, ?, ?,
                    GETDATE(),
                    GETDATE()
                )
                """;

        try (
                Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getPhone());
            ps.setString(3, supplier.getAddress());
            ps.setString(4, supplier.getStatus());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Cập nhật supplier
     */
    public boolean update(Supplier supplier) {

        String sql = """
                UPDATE Supplier
                SET
                    Name = ?,
                    Phone = ?,
                    Address = ?,
                    Status = ?,
                    UpdatedAt = GETDATE()
                WHERE SupplierID = ?
                """;

        try (
                Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getPhone());
            ps.setString(3, supplier.getAddress());
            ps.setString(4, supplier.getStatus());
            ps.setInt(5, supplier.getSupplierID());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Xóa supplier
     */
    public boolean delete(int supplierId) {

        String sql = """
                UPDATE Supplier SET Status = 'inactive'
                WHERE SupplierID = ?
                """;

        try (
                Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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
                FROM Supplier
                WHERE Status = 'active'
                """;

        try (
                Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

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
                FROM Supplier
                WHERE Status = 'inactive'
                """;

        try (
                Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
