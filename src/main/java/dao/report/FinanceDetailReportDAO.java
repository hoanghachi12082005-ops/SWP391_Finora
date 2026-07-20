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
import model.Payment;
import model.FinanceDetailReportOverview;
import util.database.DBContext;

public class FinanceDetailReportDAO {

    private static final String SELECT_PAYMENT =
            "SELECT " +
            "    p.payment_id AS PaymentID, " +
            "    p.order_id AS OrderID, " +
            "    p.payment_amount AS PaymentAmount, " +
            "    p.payment_date AS PaymentDate, " +
            "    p.payment_status AS PaymentStatus, " +
            "    p.transaction_code AS TransactionCode, " +
            "    p.PaymentType AS PaymentType, " +
            "    p.Description AS Description, " +
            "    p.EmployeeID AS EmployeeID, " +
            "    p.BranchID AS BranchID, " +
            "    p.payment_method AS PaymentMethod, " +
            "    e.fullName AS CreatorName, " +
            "    b.branch_name AS BranchName ";

    private static final String FROM_PAYMENT =
            "FROM payment p " +
            "LEFT JOIN Employee e ON p.EmployeeID = e.emp_id " +
            "LEFT JOIN Branch b ON p.BranchID = b.branch_id " +
            "WHERE 1=1 ";

    public List<Payment> getFinanceDetailReport(String keyword,
                                                 String branchFilter,
                                                 String typeFilter,
                                                 LocalDate dateFrom,
                                                 LocalDate dateTo,
                                                 int page,
                                                 int pageSize) {
        List<Payment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_PAYMENT + FROM_PAYMENT);
        List<Object> params = new ArrayList<>();

        applyFilters(sql, params, keyword, branchFilter, typeFilter, dateFrom, dateTo);

        sql.append(" ORDER BY p.payment_date DESC, p.payment_id DESC ");
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            for (Object param : params) {
                if (param == null) {
                    ps.setNull(idx++, Types.NULL);
                } else if (param instanceof Date) {
                    ps.setDate(idx++, (Date) param);
                } else if (param instanceof Integer) {
                    ps.setInt(idx++, (Integer) param);
                } else {
                    ps.setString(idx++, param.toString());
                }
            }
            ps.setInt(idx++, (page - 1) * pageSize);
            ps.setInt(idx, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractPayment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countFinanceDetailReport(String keyword,
                                         String branchFilter,
                                         String typeFilter,
                                         LocalDate dateFrom,
                                         LocalDate dateTo) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS Total " + FROM_PAYMENT);
        List<Object> params = new ArrayList<>();

        applyFilters(sql, params, keyword, branchFilter, typeFilter, dateFrom, dateTo);

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            for (Object param : params) {
                if (param == null) {
                    ps.setNull(idx++, Types.NULL);
                } else if (param instanceof Date) {
                    ps.setDate(idx++, (Date) param);
                } else if (param instanceof Integer) {
                    ps.setInt(idx++, (Integer) param);
                } else {
                    ps.setString(idx++, param.toString());
                }
            }

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

    public FinanceDetailReportOverview getReportOverview(String keyword,
                                                         String branchFilter,
                                                         String typeFilter,
                                                         LocalDate dateFrom,
                                                         LocalDate dateTo) {
        FinanceDetailReportOverview overview = new FinanceDetailReportOverview();
        
        StringBuilder sql = new StringBuilder("""
            SELECT 
                COUNT(p.payment_id) AS TotalTransactions,
                COALESCE(SUM(CASE WHEN p.PaymentType = 'INCOME' THEN p.payment_amount ELSE 0 END), 0) AS TotalIncome,
                COALESCE(SUM(CASE WHEN p.PaymentType = 'EXPENSE' THEN p.payment_amount ELSE 0 END), 0) AS TotalExpense
            FROM payment p
            LEFT JOIN Employee e ON p.EmployeeID = e.emp_id
            LEFT JOIN Branch b ON p.BranchID = b.branch_id
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();

        applyFilters(sql, params, keyword, branchFilter, typeFilter, dateFrom, dateTo);

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            for (Object param : params) {
                if (param == null) {
                    ps.setNull(idx++, Types.NULL);
                } else if (param instanceof Date) {
                    ps.setDate(idx++, (Date) param);
                } else if (param instanceof Integer) {
                    ps.setInt(idx++, (Integer) param);
                } else {
                    ps.setString(idx++, param.toString());
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    overview.setTotalTransactions(rs.getInt("TotalTransactions"));
                    BigDecimal income = rs.getBigDecimal("TotalIncome");
                    BigDecimal expense = rs.getBigDecimal("TotalExpense");
                    overview.setTotalIncome(income);
                    overview.setTotalExpense(expense);
                    overview.setNetProfit(income.subtract(expense));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return overview;
    }

    private void applyFilters(StringBuilder sql, List<Object> params,
                              String keyword, String branchFilter, String typeFilter,
                              LocalDate dateFrom, LocalDate dateTo) {
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (p.transaction_code LIKE ? OR p.description LIKE ?) ");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }
        if (branchFilter != null && !branchFilter.isBlank()) {
            try {
                int bid = Integer.parseInt(branchFilter);
                sql.append(" AND p.BranchID = ? ");
                params.add(bid);
            } catch (NumberFormatException ignored) {}
        }
        if (typeFilter != null && !typeFilter.isBlank()) {
            sql.append(" AND p.PaymentType = ? ");
            params.add(typeFilter);
        }
        if (dateFrom != null) {
            sql.append(" AND CAST(p.payment_date AS DATE) >= ? ");
            params.add(Date.valueOf(dateFrom));
        }
        if (dateTo != null) {
            sql.append(" AND CAST(p.payment_date AS DATE) <= ? ");
            params.add(Date.valueOf(dateTo));
        }
    }

    private Payment extractPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("PaymentID"));
        p.setName(rs.getString("TransactionCode"));
        p.setOrderId(rs.getObject("OrderID") != null ? rs.getInt("OrderID") : null);
        p.setMethod(rs.getString("PaymentMethod"));
        p.setAmount(rs.getDouble("PaymentAmount"));
        p.setPaymentDate(rs.getTimestamp("PaymentDate"));
        p.setStatus(rs.getString("PaymentStatus"));
        p.setPaymentType(rs.getString("PaymentType"));
        p.setDescription(rs.getString("Description"));
        p.setEmployeeId(rs.getObject("EmployeeID") != null ? rs.getInt("EmployeeID") : null);
        p.setBranchId(rs.getObject("BranchID") != null ? rs.getInt("BranchID") : null);
        p.setCreatorName(rs.getString("CreatorName"));
        p.setBranchName(rs.getString("BranchName"));
        return p;
    }
}
