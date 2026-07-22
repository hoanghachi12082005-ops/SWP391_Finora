package dao.report;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.LoyalCustomerOverview;
import model.LoyalCustomerSummary;
import util.database.DBContext;

public class CustomerLoyaltyReportDAO {

    private static final String CUSTOMER_SELECT =
            "SELECT " +
            "    c.cus_id, " +
            "    c.full_name, " +
            "    c.phone, " +
            "    c.email, " +
            "    c.total_spent, " +
            "    COUNT(o.order_id) AS TotalOrders, " +
            "    COALESCE(cp.current_points, 0) AS CurrentPoints ";

    private static final String CUSTOMER_FROM =
            "FROM customer c " +
            "LEFT JOIN customer_point cp ON c.cus_id = cp.cus_id " +
            "LEFT JOIN [Order] o ON c.cus_id = o.customer_id AND o.status = 'COMPLETED' " +
            "WHERE c.status = 'ACTIVE' " +
            "AND (? IS NULL OR c.full_name LIKE ? OR c.phone LIKE ? OR c.email LIKE ?) " +
            "GROUP BY c.cus_id, c.full_name, c.phone, c.email, c.total_spent, cp.current_points";

    public List<LoyalCustomerSummary> getCustomerLoyaltyReport(String keyword,
                                                               int page,
                                                               int pageSize) {
        List<LoyalCustomerSummary> list = new ArrayList<>();
        String sql = CUSTOMER_SELECT + CUSTOMER_FROM +
                " ORDER BY c.total_spent DESC, c.full_name ASC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bindSearch(ps, 1, keyword);

            int offset = (page - 1) * pageSize;
            ps.setInt(5, offset);
            ps.setInt(6, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSummary(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countCustomerLoyaltyReport(String keyword) {
        String sql = "SELECT COUNT(*) AS Total FROM ( " +
                "    SELECT c.cus_id " +
                "    FROM customer c " +
                "    WHERE c.status = 'ACTIVE' " +
                "    AND (? IS NULL OR c.full_name LIKE ? OR c.phone LIKE ? OR c.email LIKE ?) " +
                ") AS FilteredCustomers";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bindSearch(ps, 1, keyword);

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

    public LoyalCustomerOverview getReportOverview(String keyword) {
        LoyalCustomerOverview overview = new LoyalCustomerOverview();
        
        // Count and spent
        String sql = "SELECT " +
                "    COUNT(c.cus_id) AS TotalCustomers, " +
                "    SUM(c.total_spent) AS TotalSpent " +
                "FROM customer c " +
                "WHERE c.status = 'ACTIVE' " +
                "AND (? IS NULL OR c.full_name LIKE ? OR c.phone LIKE ? OR c.email LIKE ?)";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bindSearch(ps, 1, keyword);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    overview.setTotalCustomers(rs.getInt("TotalCustomers"));
                    BigDecimal spent = rs.getBigDecimal("TotalSpent");
                    overview.setTotalSpent(spent == null ? BigDecimal.ZERO : spent);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Top Customer
        String topSql = "SELECT TOP 1 c.full_name, c.total_spent " +
                "FROM customer c " +
                "WHERE c.status = 'ACTIVE' " +
                "AND (? IS NULL OR c.full_name LIKE ? OR c.phone LIKE ? OR c.email LIKE ?) " +
                "ORDER BY c.total_spent DESC";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(topSql)) {
            bindSearch(ps, 1, keyword);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    overview.setTopCustomerName(rs.getString("full_name"));
                    overview.setTopCustomerSpent(rs.getBigDecimal("total_spent"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return overview;
    }

    private void bindSearch(PreparedStatement ps, int startIndex, String keyword) throws SQLException {
        if (keyword == null || keyword.isEmpty()) {
            ps.setNull(startIndex, Types.VARCHAR);
            ps.setNull(startIndex + 1, Types.VARCHAR);
            ps.setNull(startIndex + 2, Types.VARCHAR);
            ps.setNull(startIndex + 3, Types.VARCHAR);
        } else {
            String match = "%" + keyword + "%";
            ps.setString(startIndex, match);
            ps.setString(startIndex + 1, match);
            ps.setString(startIndex + 2, match);
            ps.setString(startIndex + 3, match);
        }
    }

    private LoyalCustomerSummary mapSummary(ResultSet rs) throws SQLException {
        LoyalCustomerSummary summary = new LoyalCustomerSummary();
        summary.setCustomerId(rs.getInt("cus_id"));
        summary.setFullName(rs.getString("full_name"));
        summary.setPhone(rs.getString("phone"));
        summary.setEmail(rs.getString("email"));
        summary.setTotalSpent(rs.getBigDecimal("total_spent"));
        summary.setTotalOrders(rs.getInt("TotalOrders"));
        summary.setCurrentPoints(rs.getInt("CurrentPoints"));
        return summary;
    }
}
