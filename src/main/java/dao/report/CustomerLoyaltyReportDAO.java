package dao.report;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.LoyalCustomerOverview;
import model.LoyalCustomerSummary;
import util.database.DBContext;

public class CustomerLoyaltyReportDAO {

    public List<LoyalCustomerSummary> getCustomerLoyaltyReport(String keyword,
                                                               int page,
                                                               int pageSize,
                                                               Integer branchId,
                                                               LocalDate dateFrom,
                                                               LocalDate dateTo) {
        List<LoyalCustomerSummary> list = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
           .append("    c.cus_id, ")
           .append("    c.full_name, ")
           .append("    c.phone, ")
           .append("    c.email, ")
           .append("    COALESCE(SUM(o.total_amount), 0) AS total_spent, ")
           .append("    COUNT(o.order_id) AS TotalOrders, ")
           .append("    COALESCE(cp.current_points, 0) AS CurrentPoints ")
           .append("FROM customer c ")
           .append("LEFT JOIN customer_point cp ON c.cus_id = cp.cus_id ")
           .append("LEFT JOIN [Order] o ON c.cus_id = o.customer_id AND o.status = 'COMPLETED' ")
           .append("    AND (? IS NULL OR o.branch_id = ?) ")
           .append("    AND (? IS NULL OR o.created_at >= ?) ")
           .append("    AND (? IS NULL OR o.created_at <= ?) ")
           .append("WHERE c.status = 'ACTIVE' ")
           .append("AND (? IS NULL OR c.full_name LIKE ? OR c.phone LIKE ? OR c.email LIKE ?) ")
           .append("GROUP BY c.cus_id, c.full_name, c.phone, c.email, cp.current_points ");

        boolean hasFilters = (branchId != null && branchId > 0) || dateFrom != null || dateTo != null;
        if (hasFilters) {
            sql.append("HAVING COUNT(o.order_id) > 0 ");
        }

        sql.append("ORDER BY total_spent DESC, c.full_name ASC ")
           .append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int idx = 1;
            idx = bindBranchId(ps, idx, branchId);
            idx = bindDate(ps, idx, dateFrom);
            idx = bindDate(ps, idx, dateTo);
            idx = bindSearch(ps, idx, keyword);

            int offset = (page - 1) * pageSize;
            ps.setInt(idx++, offset);
            ps.setInt(idx, pageSize);

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

    public int countCustomerLoyaltyReport(String keyword, Integer branchId, LocalDate dateFrom, LocalDate dateTo) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) AS Total FROM ( ")
           .append("    SELECT c.cus_id ")
           .append("    FROM customer c ")
           .append("    LEFT JOIN [Order] o ON c.cus_id = o.customer_id AND o.status = 'COMPLETED' ")
           .append("        AND (? IS NULL OR o.branch_id = ?) ")
           .append("        AND (? IS NULL OR o.created_at >= ?) ")
           .append("        AND (? IS NULL OR o.created_at <= ?) ")
           .append("    WHERE c.status = 'ACTIVE' ")
           .append("    AND (? IS NULL OR c.full_name LIKE ? OR c.phone LIKE ? OR c.email LIKE ?) ")
           .append("    GROUP BY c.cus_id ");

        boolean hasFilters = (branchId != null && branchId > 0) || dateFrom != null || dateTo != null;
        if (hasFilters) {
            sql.append("HAVING COUNT(o.order_id) > 0 ");
        }
        sql.append(") AS FilteredCustomers");

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int idx = 1;
            idx = bindBranchId(ps, idx, branchId);
            idx = bindDate(ps, idx, dateFrom);
            idx = bindDate(ps, idx, dateTo);
            bindSearch(ps, idx, keyword);

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

    public LoyalCustomerOverview getReportOverview(String keyword, Integer branchId, LocalDate dateFrom, LocalDate dateTo) {
        LoyalCustomerOverview overview = new LoyalCustomerOverview();
        boolean hasFilters = (branchId != null && branchId > 0) || dateFrom != null || dateTo != null;

        // Count and spent
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
           .append("    COUNT(DISTINCT Filtered.cus_id) AS TotalCustomers, ")
           .append("    COALESCE(SUM(Filtered.total_spent), 0) AS TotalSpent ")
           .append("FROM ( ")
           .append("    SELECT c.cus_id, COALESCE(SUM(o.total_amount), 0) AS total_spent ")
           .append("    FROM customer c ")
           .append("    LEFT JOIN [Order] o ON c.cus_id = o.customer_id AND o.status = 'COMPLETED' ")
           .append("        AND (? IS NULL OR o.branch_id = ?) ")
           .append("        AND (? IS NULL OR o.created_at >= ?) ")
           .append("        AND (? IS NULL OR o.created_at <= ?) ")
           .append("    WHERE c.status = 'ACTIVE' ")
           .append("    AND (? IS NULL OR c.full_name LIKE ? OR c.phone LIKE ? OR c.email LIKE ?) ")
           .append("    GROUP BY c.cus_id ");
        if (hasFilters) {
            sql.append("HAVING COUNT(o.order_id) > 0 ");
        }
        sql.append(") AS Filtered");

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int idx = 1;
            idx = bindBranchId(ps, idx, branchId);
            idx = bindDate(ps, idx, dateFrom);
            idx = bindDate(ps, idx, dateTo);
            bindSearch(ps, idx, keyword);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    overview.setTotalCustomers(rs.getInt("TotalCustomers"));
                    overview.setTotalSpent(rs.getBigDecimal("TotalSpent"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Top Customer
        StringBuilder topSql = new StringBuilder();
        topSql.append("SELECT TOP 1 c.full_name, Filtered.total_spent ")
              .append("FROM customer c ")
              .append("JOIN ( ")
              .append("    SELECT c2.cus_id, COALESCE(SUM(o.total_amount), 0) AS total_spent ")
              .append("    FROM customer c2 ")
              .append("    LEFT JOIN [Order] o ON c2.cus_id = o.customer_id AND o.status = 'COMPLETED' ")
              .append("        AND (? IS NULL OR o.branch_id = ?) ")
              .append("        AND (? IS NULL OR o.created_at >= ?) ")
              .append("        AND (? IS NULL OR o.created_at <= ?) ")
              .append("    WHERE c2.status = 'ACTIVE' ")
              .append("    AND (? IS NULL OR c2.full_name LIKE ? OR c2.phone LIKE ? OR c2.email LIKE ?) ")
              .append("    GROUP BY c2.cus_id ");
        if (hasFilters) {
            topSql.append("HAVING COUNT(o.order_id) > 0 ");
        }
        topSql.append(") AS Filtered ON c.cus_id = Filtered.cus_id ")
              .append("ORDER BY Filtered.total_spent DESC");

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(topSql.toString())) {
            int idx = 1;
            idx = bindBranchId(ps, idx, branchId);
            idx = bindDate(ps, idx, dateFrom);
            idx = bindDate(ps, idx, dateTo);
            bindSearch(ps, idx, keyword);

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

    private int bindSearch(PreparedStatement ps, int startIndex, String keyword) throws SQLException {
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
        return startIndex + 4;
    }

    private int bindBranchId(PreparedStatement ps, int startIndex, Integer branchId) throws SQLException {
        if (branchId == null || branchId <= 0) {
            ps.setNull(startIndex, Types.INTEGER);
            ps.setNull(startIndex + 1, Types.INTEGER);
        } else {
            ps.setInt(startIndex, branchId);
            ps.setInt(startIndex + 1, branchId);
        }
        return startIndex + 2;
    }

    private int bindDate(PreparedStatement ps, int startIndex, LocalDate date) throws SQLException {
        if (date == null) {
            ps.setNull(startIndex, Types.DATE);
            ps.setNull(startIndex + 1, Types.DATE);
        } else {
            java.sql.Date sqlDate = java.sql.Date.valueOf(date);
            ps.setDate(startIndex, sqlDate);
            ps.setDate(startIndex + 1, sqlDate);
        }
        return startIndex + 2;
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
