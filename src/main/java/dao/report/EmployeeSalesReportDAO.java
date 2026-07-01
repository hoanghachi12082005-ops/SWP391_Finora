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
import model.EmployeeOverview;
import model.EmployeeSalesSummary;
import util.database.DBContext;

public class EmployeeSalesReportDAO {

    private static final String EMPLOYEE_FROM =
            "FROM Employee e " +
            "LEFT JOIN [Role] r ON e.role_id = r.role_id " +
            "LEFT JOIN Branch b ON e.branch_id = b.branch_id ";

    private static final String EMPLOYEE_SCOPE =
            EMPLOYEE_FROM +
            "WHERE (r.role_name IS NULL OR r.role_name NOT IN ('Admin', 'Owner')) ";

    private static final String REPORT_SELECT =
            "SELECT " +
            "    e.emp_id, " +
            "    e.fullName, " +
            "    b.branch_name AS BranchName, " +
            "    r.role_name AS RoleName, " +
            "    COUNT(o.order_id) AS TotalOrders, " +
            "    COALESCE(SUM(o.total_amount), 0) AS TotalRevenue, " +
            "    COALESCE(AVG(o.total_amount), 0) AS AverageOrderValue, " +
            "    COUNT(CASE WHEN o.status = 'COMPLETED' THEN 1 END) AS CompletedOrders, " +
            "    COUNT(CASE WHEN o.status = 'CANCELLED' THEN 1 END) AS CancelledOrders ";

    private static final String REPORT_FROM =
            EMPLOYEE_FROM +
            "LEFT JOIN [Order] o ON e.emp_id = o.emp_id " +
            "    AND (? IS NULL OR CAST(o.created_at AS DATE) >= ?) " +
            "    AND (? IS NULL OR CAST(o.created_at AS DATE) <= ?) " +
            "WHERE (r.role_name IS NULL OR r.role_name NOT IN ('Admin', 'Owner')) " +
            "AND (? IS NULL OR e.fullName LIKE ? OR e.email LIKE ? OR e.phone LIKE ?) " +
            "AND (? IS NULL OR e.branch_id = ?) " +
            "GROUP BY e.emp_id, e.fullName, b.branch_name, r.role_name";

    public List<EmployeeSalesSummary> getEmployeeSalesReport(String keyword,
                                                              String branchFilter,
                                                              LocalDate dateFrom,
                                                              LocalDate dateTo,
                                                              int page,
                                                              int pageSize) {
        List<EmployeeSalesSummary> list = new ArrayList<>();

        String sql = REPORT_SELECT +
                REPORT_FROM +
                " ORDER BY TotalRevenue DESC, fullName ASC " +
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

    public List<EmployeeSalesSummary> getAllEmployeeSalesReport(String keyword,
                                                                 String branchFilter,
                                                                 LocalDate dateFrom,
                                                                 LocalDate dateTo) {
        List<EmployeeSalesSummary> list = new ArrayList<>();
        String sql = REPORT_SELECT + REPORT_FROM +
                " ORDER BY TotalRevenue DESC, fullName ASC";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bindDateRange(ps, 1, dateFrom, dateTo);
            bindSearchAndBranch(ps, 5, keyword, branchFilter);

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

    public int countEmployeeSalesReport(String keyword, String branchFilter) {
        String sql =
                "SELECT COUNT(*) AS Total FROM ( " +
                "    SELECT e.emp_id " +
                EMPLOYEE_SCOPE +
                "    AND (? IS NULL OR e.fullName LIKE ? OR e.email LIKE ? OR e.phone LIKE ?) " +
                "    AND (? IS NULL OR e.branch_id = ?) " +
                "    GROUP BY e.emp_id " +
                ") AS FilteredEmployees";

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

    public EmployeeOverview getReportOverview(String keyword,
                                              String branchFilter,
                                              LocalDate dateFrom,
                                              LocalDate dateTo) {
        EmployeeOverview overview = new EmployeeOverview();
        loadEmployeeCounts(overview, keyword, branchFilter);
        loadOrderMetrics(overview, keyword, branchFilter, dateFrom, dateTo);
        loadTopEmployee(overview, keyword, branchFilter, dateFrom, dateTo);
        return overview;
    }

    private void loadEmployeeCounts(EmployeeOverview overview,
                                    String keyword,
                                    String branchFilter) {
        String sql =
                "SELECT " +
                "    COUNT(DISTINCT e.emp_id) AS TotalEmployees, " +
                "    COUNT(DISTINCT CASE WHEN LOWER(e.status) = 'active' THEN e.emp_id END) AS ActiveEmployees " +
                EMPLOYEE_SCOPE +
                "AND (? IS NULL OR e.fullName LIKE ? OR e.email LIKE ? OR e.phone LIKE ?) " +
                "AND (? IS NULL OR e.branch_id = ?)";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bindSearchAndBranch(ps, 1, keyword, branchFilter);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    overview.setTotalEmployees(rs.getInt("TotalEmployees"));
                    overview.setActiveEmployees(rs.getInt("ActiveEmployees"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadOrderMetrics(EmployeeOverview overview,
                                  String keyword,
                                  String branchFilter,
                                  LocalDate dateFrom,
                                  LocalDate dateTo) {
        String sql =
                "SELECT " +
                "    COUNT(o.order_id) AS TotalOrders, " +
                "    COALESCE(SUM(o.total_amount), 0) AS TotalRevenue " +
                "FROM [Order] o " +
                "JOIN Employee e ON o.emp_id = e.emp_id " +
                "LEFT JOIN [Role] r ON e.role_id = r.role_id " +
                "WHERE (r.role_name IS NULL OR r.role_name NOT IN ('Admin', 'Owner')) " +
                "AND (? IS NULL OR e.fullName LIKE ? OR e.email LIKE ? OR e.phone LIKE ?) " +
                "AND (? IS NULL OR e.branch_id = ?) " +
                "AND (? IS NULL OR CAST(o.created_at AS DATE) >= ?) " +
                "AND (? IS NULL OR CAST(o.created_at AS DATE) <= ?)";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bindSearchAndBranch(ps, 1, keyword, branchFilter);
            bindDateRange(ps, 7, dateFrom, dateTo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    overview.setTotalOrders(rs.getInt("TotalOrders"));
                    BigDecimal revenue = rs.getBigDecimal("TotalRevenue");
                    overview.setTotalRevenue(revenue == null ? BigDecimal.ZERO : revenue);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTopEmployee(EmployeeOverview overview,
                                  String keyword,
                                  String branchFilter,
                                  LocalDate dateFrom,
                                  LocalDate dateTo) {
        String topSql =
                "SELECT TOP 1 " +
                "    e.fullName, " +
                "    COALESCE(SUM(o.total_amount), 0) AS EmployeeRevenue " +
                EMPLOYEE_FROM +
                "LEFT JOIN [Order] o ON e.emp_id = o.emp_id " +
                "    AND (? IS NULL OR CAST(o.created_at AS DATE) >= ?) " +
                "    AND (? IS NULL OR CAST(o.created_at AS DATE) <= ?) " +
                "WHERE (r.role_name IS NULL OR r.role_name NOT IN ('Admin', 'Owner')) " +
                "AND (? IS NULL OR e.fullName LIKE ? OR e.email LIKE ? OR e.phone LIKE ?) " +
                "AND (? IS NULL OR e.branch_id = ?) " +
                "GROUP BY e.emp_id, e.fullName " +
                "ORDER BY EmployeeRevenue DESC, e.fullName ASC";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(topSql)) {
            bindDateRange(ps, 1, dateFrom, dateTo);
            bindSearchAndBranch(ps, 5, keyword, branchFilter);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    overview.setTopEmployeeName(rs.getString("FullName"));
                    BigDecimal revenue = rs.getBigDecimal("EmployeeRevenue");
                    overview.setTopEmployeeRevenue(revenue == null ? BigDecimal.ZERO : revenue);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String lowestSql =
                "SELECT TOP 1 " +
                "    e.fullName, " +
                "    COALESCE(SUM(o.total_amount), 0) AS EmployeeRevenue " +
                EMPLOYEE_FROM +
                "LEFT JOIN [Order] o ON e.emp_id = o.emp_id " +
                "    AND (? IS NULL OR CAST(o.created_at AS DATE) >= ?) " +
                "    AND (? IS NULL OR CAST(o.created_at AS DATE) <= ?) " +
                "WHERE (r.role_name IS NULL OR r.role_name NOT IN ('Admin', 'Owner')) " +
                "AND (? IS NULL OR e.fullName LIKE ? OR e.email LIKE ? OR e.phone LIKE ?) " +
                "AND (? IS NULL OR e.branch_id = ?) " +
                "GROUP BY e.emp_id, e.fullName " +
                "HAVING COALESCE(SUM(o.total_amount), 0) > 0 " +
                "ORDER BY EmployeeRevenue ASC, e.fullName ASC";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement ps = connection.prepareStatement(lowestSql)) {
            bindDateRange(ps, 1, dateFrom, dateTo);
            bindSearchAndBranch(ps, 5, keyword, branchFilter);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    overview.setLowestEmployeeName(rs.getString("FullName"));
                    BigDecimal revenue = rs.getBigDecimal("EmployeeRevenue");
                    overview.setLowestEmployeeRevenue(revenue == null ? BigDecimal.ZERO : revenue);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void bindSearchAndBranch(PreparedStatement ps,
                                     int startIndex,
                                     String keyword,
                                     String branchFilter) throws SQLException {
        String searchValue = normalizeLike(keyword);
        Integer branchId = parseInteger(branchFilter);

        ps.setString(startIndex, searchValue);
        ps.setString(startIndex + 1, searchValue);
        ps.setString(startIndex + 2, searchValue);
        ps.setString(startIndex + 3, searchValue);

        if (branchId == null) {
            ps.setNull(startIndex + 4, Types.INTEGER);
            ps.setNull(startIndex + 5, Types.INTEGER);
        } else {
            ps.setInt(startIndex + 4, branchId);
            ps.setInt(startIndex + 5, branchId);
        }
    }

    private void bindDateRange(PreparedStatement ps,
                               int startIndex,
                               LocalDate dateFrom,
                               LocalDate dateTo) throws SQLException {
        if (dateFrom == null) {
            ps.setNull(startIndex, Types.DATE);
            ps.setNull(startIndex + 1, Types.DATE);
        } else {
            ps.setDate(startIndex, Date.valueOf(dateFrom));
            ps.setDate(startIndex + 1, Date.valueOf(dateFrom));
        }

        if (dateTo == null) {
            ps.setNull(startIndex + 2, Types.DATE);
            ps.setNull(startIndex + 3, Types.DATE);
        } else {
            ps.setDate(startIndex + 2, Date.valueOf(dateTo));
            ps.setDate(startIndex + 3, Date.valueOf(dateTo));
        }
    }

    private EmployeeSalesSummary mapSummary(ResultSet rs) throws SQLException {
        EmployeeSalesSummary summary = new EmployeeSalesSummary();
        summary.setEmployeeId(rs.getInt("emp_id"));
        summary.setFullName(rs.getString("fullName"));
        summary.setBranchName(rs.getString("BranchName"));
        summary.setRoleName(rs.getString("RoleName"));
        summary.setTotalOrders(rs.getInt("TotalOrders"));

        BigDecimal revenue = rs.getBigDecimal("TotalRevenue");
        summary.setTotalRevenue(revenue == null ? BigDecimal.ZERO : revenue);

        BigDecimal average = rs.getBigDecimal("AverageOrderValue");
        summary.setAverageOrderValue(average == null ? BigDecimal.ZERO : average);

        summary.setCompletedOrders(rs.getInt("CompletedOrders"));
        summary.setCancelledOrders(rs.getInt("CancelledOrders"));

        return summary;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
}
