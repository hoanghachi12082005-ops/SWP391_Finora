package dao.sales;

import model.Shift;
import util.database.DBContext;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftDAO {

    public Shift getOpenShiftByEmp(int empId) {
        String sql = """
            SELECT s.*, e.fullName AS employeeName, b.branch_name AS branchName
            FROM shift s
            JOIN Employee e ON s.emp_id = e.emp_id
            JOIN Branch b ON s.branch_id = b.branch_id
            WHERE s.emp_id = ? AND s.status = 'OPEN'
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Shift s = mapRow(rs);
                    s.setEmployeeName(rs.getString("employeeName"));
                    s.setBranchName(rs.getString("branchName"));
                    return s;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int openShift(int empId, int branchId, BigDecimal openingCash) {
        String sql = """
            INSERT INTO shift (emp_id, branch_id, opening_cash, expected_cash, status, opened_at)
            VALUES (?, ?, ?, ?, 'OPEN', GETDATE())
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, empId);
            ps.setInt(2, branchId);
            ps.setBigDecimal(3, openingCash);
            ps.setBigDecimal(4, openingCash); // initial expected cash is opening cash
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean closeShift(int shiftId, BigDecimal closingCash) {
        BigDecimal expectedCash = getExpectedCash(shiftId);
        String sql = """
            UPDATE shift
            SET status = 'CLOSED', closed_at = GETDATE(), closing_cash = ?, expected_cash = ?
            WHERE shift_id = ?
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, closingCash);
            ps.setBigDecimal(2, expectedCash);
            ps.setInt(3, shiftId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public BigDecimal getExpectedCash(int shiftId) {
        BigDecimal openingCash = BigDecimal.ZERO;
        Timestamp openedAt = null;
        Timestamp closedAt = null;
        int empId = 0;
        int branchId = 0;

        // 1. Get shift details
        String shiftSql = "SELECT opening_cash, opened_at, closed_at, emp_id, branch_id FROM shift WHERE shift_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(shiftSql)) {
            ps.setInt(1, shiftId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    openingCash = rs.getBigDecimal("opening_cash");
                    openedAt = rs.getTimestamp("opened_at");
                    closedAt = rs.getTimestamp("closed_at");
                    empId = rs.getInt("emp_id");
                    branchId = rs.getInt("branch_id");
                } else {
                    return BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }

        if (openedAt == null) return BigDecimal.ZERO;
        if (closedAt == null) {
            closedAt = new Timestamp(System.currentTimeMillis());
        }

        // 2. Sum up completed CASH sales
        BigDecimal cashSales = BigDecimal.ZERO;
        String salesSql = """
            SELECT COALESCE(SUM(total_amount), 0) AS total_cash_sales
            FROM [order]
            WHERE emp_id = ? 
              AND branch_id = ?
              AND order_type = 'SALE'
              AND payment_method = 'CASH'
              AND status = 'COMPLETED'
              AND created_at BETWEEN ? AND ?
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(salesSql)) {
            ps.setInt(1, empId);
            ps.setInt(2, branchId);
            ps.setTimestamp(3, openedAt);
            ps.setTimestamp(4, closedAt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cashSales = rs.getBigDecimal("total_cash_sales");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 3. Get total withdraws and deposits
        BigDecimal totalDeposit = BigDecimal.ZERO;
        BigDecimal totalWithdraw = BigDecimal.ZERO;
        String txSql = """
            SELECT 
                COALESCE(SUM(CASE WHEN type = 'DEPOSIT' THEN amount ELSE 0 END), 0) AS total_deposit,
                COALESCE(SUM(CASE WHEN type = 'WITHDRAW' THEN amount ELSE 0 END), 0) AS total_withdraw
            FROM cash_transaction
            WHERE shift_id = ?
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(txSql)) {
            ps.setInt(1, shiftId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalDeposit = rs.getBigDecimal("total_deposit");
                    totalWithdraw = rs.getBigDecimal("total_withdraw");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // expected_cash = opening_cash + cashSales - totalWithdraw + totalDeposit
        return openingCash.add(cashSales).subtract(totalWithdraw).add(totalDeposit);
    }

    public Map<String, Object> getShiftSummary(int shiftId) {
        Map<String, Object> summary = new HashMap<>();
        BigDecimal openingCash = BigDecimal.ZERO;
        Timestamp openedAt = null;
        Timestamp closedAt = null;
        int empId = 0;
        int branchId = 0;
        String status = "CLOSED";
        BigDecimal closingCash = BigDecimal.ZERO;

        String shiftSql = """
            SELECT s.*, e.fullName AS employeeName, b.branch_name AS branchName
            FROM shift s
            JOIN Employee e ON s.emp_id = e.emp_id
            JOIN Branch b ON s.branch_id = b.branch_id
            WHERE s.shift_id = ?
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(shiftSql)) {
            ps.setInt(1, shiftId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    openingCash = rs.getBigDecimal("opening_cash");
                    closingCash = rs.getBigDecimal("closing_cash");
                    openedAt = rs.getTimestamp("opened_at");
                    closedAt = rs.getTimestamp("closed_at");
                    empId = rs.getInt("emp_id");
                    branchId = rs.getInt("branch_id");
                    status = rs.getString("status");
                    summary.put("employeeName", rs.getString("employeeName"));
                    summary.put("branchName", rs.getString("branchName"));
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        summary.put("shiftId", shiftId);
        summary.put("status", status);
        summary.put("openingCash", openingCash);
        summary.put("closingCash", closingCash);
        summary.put("openedAt", openedAt != null ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(openedAt) : "");
        summary.put("closedAt", closedAt != null ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(closedAt) : "");

        Timestamp endRange = (closedAt != null) ? closedAt : new Timestamp(System.currentTimeMillis());

        // Cash Sales
        BigDecimal cashSales = BigDecimal.ZERO;
        String salesSql = """
            SELECT COALESCE(SUM(total_amount), 0) AS total_cash_sales
            FROM [order]
            WHERE emp_id = ? 
              AND branch_id = ?
              AND order_type = 'SALE'
              AND payment_method = 'CASH'
              AND status = 'COMPLETED'
              AND created_at BETWEEN ? AND ?
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(salesSql)) {
            ps.setInt(1, empId);
            ps.setInt(2, branchId);
            ps.setTimestamp(3, openedAt);
            ps.setTimestamp(4, endRange);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cashSales = rs.getBigDecimal("total_cash_sales");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        summary.put("cashSales", cashSales);

        // Card/Bank Transfer sales (non-cash)
        BigDecimal bankSales = BigDecimal.ZERO;
        String bankSql = """
            SELECT COALESCE(SUM(total_amount), 0) AS total_bank_sales
            FROM [order]
            WHERE emp_id = ? 
              AND branch_id = ?
              AND order_type = 'SALE'
              AND payment_method = 'BANK_TRANSFER'
              AND status = 'COMPLETED'
              AND created_at BETWEEN ? AND ?
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(bankSql)) {
            ps.setInt(1, empId);
            ps.setInt(2, branchId);
            ps.setTimestamp(3, openedAt);
            ps.setTimestamp(4, endRange);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    bankSales = rs.getBigDecimal("total_bank_sales");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        summary.put("bankSales", bankSales);
        summary.put("totalRevenue", cashSales.add(bankSales));

        // Cash transactions
        BigDecimal totalDeposit = BigDecimal.ZERO;
        BigDecimal totalWithdraw = BigDecimal.ZERO;
        String txSql = """
            SELECT 
                COALESCE(SUM(CASE WHEN type = 'DEPOSIT' THEN amount ELSE 0 END), 0) AS total_deposit,
                COALESCE(SUM(CASE WHEN type = 'WITHDRAW' THEN amount ELSE 0 END), 0) AS total_withdraw
            FROM cash_transaction
            WHERE shift_id = ?
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(txSql)) {
            ps.setInt(1, shiftId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalDeposit = rs.getBigDecimal("total_deposit");
                    totalWithdraw = rs.getBigDecimal("total_withdraw");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        summary.put("totalDeposit", totalDeposit);
        summary.put("totalWithdraw", totalWithdraw);

        // Expected Cash
        BigDecimal expectedCash = openingCash.add(cashSales).subtract(totalWithdraw).add(totalDeposit);
        summary.put("expectedCash", expectedCash);

        return summary;
    }

    public List<Shift> getShiftHistory(int branchId, int limit) {
        List<Shift> list = new ArrayList<>();
        String sql = """
            SELECT TOP (?) s.*, e.fullName AS employeeName, b.branch_name AS branchName
            FROM shift s
            JOIN Employee e ON s.emp_id = e.emp_id
            JOIN Branch b ON s.branch_id = b.branch_id
            WHERE s.branch_id = ?
            ORDER BY s.shift_id DESC
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Shift s = mapRow(rs);
                    s.setEmployeeName(rs.getString("employeeName"));
                    s.setBranchName(rs.getString("branchName"));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Shift mapRow(ResultSet rs) throws SQLException {
        Shift s = new Shift();
        s.setShiftId(rs.getInt("shift_id"));
        s.setEmpId(rs.getInt("emp_id"));
        s.setBranchId(rs.getInt("branch_id"));
        s.setOpeningCash(rs.getBigDecimal("opening_cash"));
        s.setClosingCash(rs.getBigDecimal("closing_cash"));
        s.setExpectedCash(rs.getBigDecimal("expected_cash"));
        s.setStatus(rs.getString("status"));
        
        Timestamp opened = rs.getTimestamp("opened_at");
        if (opened != null) {
            s.setOpenedAt(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(opened));
        }
        Timestamp closed = rs.getTimestamp("closed_at");
        if (closed != null) {
            s.setClosedAt(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(closed));
        }
        return s;
    }
}
