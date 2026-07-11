package dao.report;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.BranchSalesOverview;
import model.BranchSalesSummary;
import util.database.DBContext;

public class BranchSalesReportDAO {

    private static final String BRANCH_SELECT =
            "SELECT " +
            "    b.branch_id, " +
            "    b.branch_name, " +
            "    b.address, " +
            "    COUNT(o.order_id) AS TotalOrders, " +
            "    COALESCE(SUM(o.total_amount), 0) AS TotalRevenue, " +
            "    COALESCE(AVG(o.total_amount), 0) AS AverageOrderValue, " +
            "    COUNT(CASE WHEN o.status = 'COMPLETED' THEN 1 END) AS CompletedOrders, " +
            "    COUNT(CASE WHEN o.status = 'CANCELLED' THEN 1 END) AS CancelledOrders ";

    private static final String BRANCH_FROM =
            "FROM Branch b " +
            "LEFT JOIN [Order] o ON b.branch_id = o.branch_id " +
            "    AND (? IS NULL OR CAST(o.created_at AS DATE) >= ?) " +
            "    AND (? IS NULL OR CAST(o.created_at AS DATE) <= ?) " +
            "WHERE (? IS NULL OR b.branch_name LIKE ? OR b.branch_code LIKE ? OR b.address LIKE ?) " +
            "AND (? IS NULL OR b.branch_id = ?) " +
            "GROUP BY b.branch_id, b.branch_name, b.address";

    public List<BranchSalesSummary> getBranchSalesReport(String keyword,
                                                           String branchFilter,
                                                           LocalDate dateFrom,
                                                           LocalDate dateTo,
                                                           int page,
                                                           int pageSize) {
        List<BranchSalesSummary> list = new ArrayList<>();
        String sql = BRANCH_SELECT + BRANCH_FROM +
                " ORDER BY TotalRevenue DESC, b.branch_name ASC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bindDateRange(ps, 1, dateFrom, dateTo);
            bindSearchAndBranch(ps, 5, keyword, branchFilter);

            int offset = (page - 1) * pageSize;
            ps.setInt(11, offset);
            ps.setInt(12, pageSize);

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

    public int countBranchSalesReport(String keyword, String branchFilter) {
        String sql =
                "SELECT COUNT(*) AS Total FROM ( " +
                "    SELECT b.branch_id FROM Branch b " +
                "    WHERE (? IS NULL OR b.branch_name LIKE ? OR b.branch_code LIKE ? OR b.address LIKE ?) " +
                "    AND (? IS NULL OR b.branch_id = ?) " +
                ") AS FilteredBranches";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bindSearchAndBranch(ps, 1, keyword, branchFilter);

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

    public BranchSalesOverview getReportOverview(String keyword,
                                                 String branchFilter,
                                                 LocalDate dateFrom,
                                                 LocalDate dateTo) {
        BranchSalesOverview overview = new BranchSalesOverview();
        
        // Count branches
        String countSql = "SELECT COUNT(DISTINCT b.branch_id) AS TotalBranches " +
                "FROM Branch b WHERE (? IS NULL OR b.branch_name LIKE ? OR b.branch_code LIKE ? OR b.address LIKE ?) " +
                "AND (? IS NULL OR b.branch_id = ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql)) {
            bindSearchAndBranch(ps, 1, keyword, branchFilter);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    overview.setTotalBranches(rs.getInt("TotalBranches"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Metrics
        String metricsSql = "SELECT COUNT(o.order_id) AS TotalOrders, COALESCE(SUM(o.total_amount), 0) AS TotalRevenue " +
                "FROM [Order] o " +
                "JOIN Branch b ON o.branch_id = b.branch_id " +
                "WHERE (? IS NULL OR b.branch_name LIKE ? OR b.branch_code LIKE ? OR b.address LIKE ?) " +
                "AND (? IS NULL OR b.branch_id = ?) " +
                "AND (? IS NULL OR CAST(o.created_at AS DATE) >= ?) " +
                "AND (? IS NULL OR CAST(o.created_at AS DATE) <= ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(metricsSql)) {
            bindSearchAndBranch(ps, 1, keyword, branchFilter);
            bindDateRange(ps, 7, dateFrom, dateTo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    overview.setTotalOrders(rs.getInt("TotalOrders"));
                    overview.setTotalRevenue(rs.getBigDecimal("TotalRevenue"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Top branch
        String topSql = "SELECT TOP 1 b.branch_name, COALESCE(SUM(o.total_amount), 0) AS BranchRevenue " +
                "FROM Branch b " +
                "LEFT JOIN [Order] o ON b.branch_id = o.branch_id " +
                "    AND (? IS NULL OR CAST(o.created_at AS DATE) >= ?) " +
                "    AND (? IS NULL OR CAST(o.created_at AS DATE) <= ?) " +
                "WHERE (? IS NULL OR b.branch_name LIKE ? OR b.branch_code LIKE ? OR b.address LIKE ?) " +
                "AND (? IS NULL OR b.branch_id = ?) " +
                "GROUP BY b.branch_name ORDER BY BranchRevenue DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(topSql)) {
            bindDateRange(ps, 1, dateFrom, dateTo);
            bindSearchAndBranch(ps, 5, keyword, branchFilter);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    overview.setTopBranchName(rs.getString("branch_name"));
                    overview.setTopBranchRevenue(rs.getBigDecimal("BranchRevenue"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return overview;
    }

    private void bindDateRange(PreparedStatement ps, int startIndex, LocalDate dateFrom, LocalDate dateTo)
            throws SQLException {
        if (dateFrom == null) {
            ps.setNull(startIndex, Types.DATE);
            ps.setNull(startIndex + 1, Types.DATE);
        } else {
            Date d = Date.valueOf(dateFrom);
            ps.setDate(startIndex, d);
            ps.setDate(startIndex + 1, d);
        }

        if (dateTo == null) {
            ps.setNull(startIndex + 2, Types.DATE);
            ps.setNull(startIndex + 3, Types.DATE);
        } else {
            Date d = Date.valueOf(dateTo);
            ps.setDate(startIndex + 2, d);
            ps.setDate(startIndex + 3, d);
        }
    }

    private void bindSearchAndBranch(PreparedStatement ps, int startIndex, String keyword, String branchFilter)
            throws SQLException {
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

        if (branchFilter == null || branchFilter.isEmpty() || "-1".equals(branchFilter)) {
            ps.setNull(startIndex + 4, Types.INTEGER);
            ps.setNull(startIndex + 5, Types.INTEGER);
        } else {
            int bid = Integer.parseInt(branchFilter);
            ps.setInt(startIndex + 4, bid);
            ps.setInt(startIndex + 5, bid);
        }
    }

    private BranchSalesSummary mapSummary(ResultSet rs) throws SQLException {
        BranchSalesSummary summary = new BranchSalesSummary();
        summary.setBranchId(rs.getInt("branch_id"));
        summary.setBranchName(rs.getString("branch_name"));
        summary.setAddress(rs.getString("address"));
        summary.setTotalOrders(rs.getInt("TotalOrders"));
        summary.setTotalRevenue(rs.getBigDecimal("TotalRevenue"));
        summary.setAverageOrderValue(rs.getBigDecimal("AverageOrderValue"));
        summary.setCompletedOrders(rs.getInt("CompletedOrders"));
        summary.setCancelledOrders(rs.getInt("CancelledOrders"));
        return summary;
    }
}
