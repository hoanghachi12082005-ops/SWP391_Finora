package dao.sales;

import model.Voucher;
import util.database.DBContext;
import java.sql.*;

/**
 * DAO thao tác bảng voucher — kiểm tra và cập nhật voucher giảm giá.
 */
public class VoucherDAO {

    /**
     * Lấy voucher còn hiệu lực theo mã code.
     * Điều kiện: status='active', ngày hiện tại trong khoảng start_date–end_date.
     * Trả về null nếu không tìm thấy hoặc hết hạn.
     */
    public Voucher getValidByCode(String code) {
        String sql = "SELECT * FROM voucher "
                   + "WHERE voucher_code = ? AND status = 'active' "
                   + "AND CAST(GETDATE() AS DATE) BETWEEN start_date AND end_date";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim());
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

    /**
     * Tăng lượt sử dụng voucher (trong cùng transaction).
     */
    public void incrementUsedQuantity(Connection conn, int voucherId) throws SQLException {
        String sql = "UPDATE voucher SET used_quantity = used_quantity + 1 WHERE voucher_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voucherId);
            ps.executeUpdate();
        }
    }

    /**
     * Lấy danh sách tất cả voucher đang còn hạn và hoạt động.
     */
    public java.util.List<Voucher> getAllValidVouchers() {
        java.util.List<Voucher> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM voucher "
                   + "WHERE status = 'active' "
                   + "AND CAST(GETDATE() AS DATE) BETWEEN start_date AND end_date "
                   + "ORDER BY voucher_code";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy voucher theo ID.
     */
    public Voucher getById(int voucherId) {
        String sql = "SELECT * FROM voucher WHERE voucher_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voucherId);
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

    private Voucher mapRow(ResultSet rs) throws SQLException {
        Voucher v = new Voucher();
        v.setVoucherId(rs.getInt("voucher_id"));
        v.setVoucherCode(rs.getString("voucher_code"));
        v.setVoucherName(rs.getString("voucher_name"));
        v.setDiscountType(rs.getString("discount_type"));
        v.setDiscountValue(rs.getDouble("discount_value"));
        v.setUsedQuantity(rs.getInt("used_quantity"));
        v.setStartDate(rs.getString("start_date"));
        v.setEndDate(rs.getString("end_date"));
        v.setStatus(rs.getString("status"));
        v.setCreatedAt(rs.getString("created_at"));
        return v;
    }
}
