package dao.sales;

import model.RevenueSummary;
import util.database.DBContext;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RevenueDAO {

    public RevenueSummary getKpiSummary(int branchId, int empId, LocalDate date) {
        String sql = """
            SELECT 
                COALESCE(SUM(total_amount), 0) AS total_rev,
                COALESCE(SUM(CASE WHEN payment_method = 'CASH' THEN total_amount ELSE 0 END), 0) AS cash_rev,
                COALESCE(SUM(CASE WHEN payment_method = 'BANK_TRANSFER' THEN total_amount ELSE 0 END), 0) AS bank_rev,
                COUNT(order_id) AS total_ord
            FROM [order]
            WHERE CAST(created_at AS DATE) = ? AND status = 'COMPLETED' AND order_type = 'SALE'
              AND (? = 0 OR branch_id = ?)
              AND (? = 0 OR emp_id = ?)
            """;

        LocalDate prevDate = date.minusDays(1);
        boolean isToday = date.equals(LocalDate.now());
        
        String sqlPrev = isToday ? """
            SELECT 
                COALESCE(SUM(total_amount), 0) AS total_rev,
                COALESCE(SUM(CASE WHEN payment_method = 'CASH' THEN total_amount ELSE 0 END), 0) AS cash_rev,
                COALESCE(SUM(CASE WHEN payment_method = 'BANK_TRANSFER' THEN total_amount ELSE 0 END), 0) AS bank_rev,
                COUNT(order_id) AS total_ord
            FROM [order]
            WHERE CAST(created_at AS DATE) = ? AND status = 'COMPLETED' AND order_type = 'SALE'
              AND CAST(created_at AS TIME) <= CAST(GETDATE() AS TIME)
              AND (? = 0 OR branch_id = ?)
              AND (? = 0 OR emp_id = ?)
            """ : """
            SELECT 
                COALESCE(SUM(total_amount), 0) AS total_rev,
                COALESCE(SUM(CASE WHEN payment_method = 'CASH' THEN total_amount ELSE 0 END), 0) AS cash_rev,
                COALESCE(SUM(CASE WHEN payment_method = 'BANK_TRANSFER' THEN total_amount ELSE 0 END), 0) AS bank_rev,
                COUNT(order_id) AS total_ord
            FROM [order]
            WHERE CAST(created_at AS DATE) = ? AND status = 'COMPLETED' AND order_type = 'SALE'
              AND (? = 0 OR branch_id = ?)
              AND (? = 0 OR emp_id = ?)
            """;

        double totalRevenue = 0, cashRevenue = 0, bankRevenue = 0, aov = 0;
        int totalOrders = 0;

        double prevTotalRevenue = 0, prevCashRevenue = 0, prevBankRevenue = 0, prevAov = 0;
        int prevTotalOrders = 0;

        // Selected Date
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setInt(2, branchId);
            ps.setInt(3, branchId);
            ps.setInt(4, empId);
            ps.setInt(5, empId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalRevenue = rs.getDouble("total_rev");
                    cashRevenue = rs.getDouble("cash_rev");
                    bankRevenue = rs.getDouble("bank_rev");
                    totalOrders = rs.getInt("total_ord");
                    aov = totalOrders > 0 ? totalRevenue / totalOrders : 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Previous Date
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlPrev)) {
            ps.setDate(1, Date.valueOf(prevDate));
            ps.setInt(2, branchId);
            ps.setInt(3, branchId);
            ps.setInt(4, empId);
            ps.setInt(5, empId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    prevTotalRevenue = rs.getDouble("total_rev");
                    prevCashRevenue = rs.getDouble("cash_rev");
                    prevBankRevenue = rs.getDouble("bank_rev");
                    prevTotalOrders = rs.getInt("total_ord");
                    prevAov = prevTotalOrders > 0 ? prevTotalRevenue / prevTotalOrders : 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        double totalRevenueChange = calculateChange(totalRevenue, prevTotalRevenue);
        double cashRevenueChange = calculateChange(cashRevenue, prevCashRevenue);
        double bankRevenueChange = calculateChange(bankRevenue, prevBankRevenue);
        double totalOrdersChange = calculateChange(totalOrders, prevTotalOrders);
        double aovChange = calculateChange(aov, prevAov);

        return new RevenueSummary(
            totalRevenue, cashRevenue, bankRevenue, totalOrders, aov,
            totalRevenueChange, cashRevenueChange, bankRevenueChange, totalOrdersChange, aovChange
        );
    }

    private double calculateChange(double current, double previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((current - previous) / previous) * 100.0;
    }

    public Map<Integer, Double[]> getRevenueByHour(int branchId, int empId, LocalDate date) {
        Map<Integer, Double[]> hourlyData = new HashMap<>();
        for (int h = 8; h <= 22; h++) {
            hourlyData.put(h, new Double[]{0.0, 0.0});
        }

        String sql = """
            SELECT DATEPART(HOUR, created_at) AS hr, SUM(total_amount) AS rev
            FROM [order]
            WHERE CAST(created_at AS DATE) = ? AND status = 'COMPLETED' AND order_type = 'SALE'
              AND (? = 0 OR branch_id = ?)
              AND (? = 0 OR emp_id = ?)
            GROUP BY DATEPART(HOUR, created_at)
            """;

        // Today
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setInt(2, branchId);
            ps.setInt(3, branchId);
            ps.setInt(4, empId);
            ps.setInt(5, empId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int hour = rs.getInt("hr");
                    if (hour >= 8 && hour <= 22) {
                        Double[] vals = hourlyData.get(hour);
                        vals[0] = rs.getDouble("rev");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Yesterday
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date.minusDays(1)));
            ps.setInt(2, branchId);
            ps.setInt(3, branchId);
            ps.setInt(4, empId);
            ps.setInt(5, empId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int hour = rs.getInt("hr");
                    if (hour >= 8 && hour <= 22) {
                        Double[] vals = hourlyData.get(hour);
                        vals[1] = rs.getDouble("rev");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return hourlyData;
    }

    public Map<String, Double> getPaymentMethodBreakdown(int branchId, int empId, LocalDate date) {
        Map<String, Double> map = new HashMap<>();
        map.put("CASH", 0.0);
        map.put("BANK_TRANSFER", 0.0);

        String sql = """
            SELECT payment_method, SUM(total_amount) AS total
            FROM [order]
            WHERE CAST(created_at AS DATE) = ? AND status = 'COMPLETED' AND order_type = 'SALE'
              AND (? = 0 OR branch_id = ?)
              AND (? = 0 OR emp_id = ?)
            GROUP BY payment_method
            """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setInt(2, branchId);
            ps.setInt(3, branchId);
            ps.setInt(4, empId);
            ps.setInt(5, empId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String method = rs.getString("payment_method");
                    if (method != null) {
                        map.put(method.toUpperCase(), rs.getDouble("total"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public List<Map<String, Object>> getTopSellingProducts(int branchId, int empId, LocalDate date, int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
            SELECT TOP (?) p.product_id, p.product_name, p.product_codebar,
                   SUM(od.quantity) AS total_qty,
                   SUM(od.total_price) AS total_rev
            FROM order_detail od
            JOIN [order] o ON od.order_id = o.order_id
            JOIN Product p ON od.product_id = p.product_id
            WHERE CAST(o.created_at AS DATE) = ? AND o.status = 'COMPLETED' AND o.order_type = 'SALE'
              AND (? = 0 OR o.branch_id = ?)
              AND (? = 0 OR o.emp_id = ?)
            GROUP BY p.product_id, p.product_name, p.product_codebar
            ORDER BY total_qty DESC
            """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setDate(2, Date.valueOf(date));
            ps.setInt(3, branchId);
            ps.setInt(4, branchId);
            ps.setInt(5, empId);
            ps.setInt(6, empId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("productId", rs.getInt("product_id"));
                    row.put("productName", rs.getString("product_name"));
                    row.put("productCode", rs.getString("product_codebar"));
                    row.put("totalQty", rs.getInt("total_qty"));
                    row.put("totalRev", rs.getDouble("total_rev"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Map<String, Object>> getRecentTransactions(int branchId, int empId, int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
            SELECT TOP (?) o.*, 
                   c.full_name AS customerName, 
                   (SELECT SUM(quantity) FROM order_detail WHERE order_id = o.order_id) AS total_items
            FROM [order] o
            LEFT JOIN Customer c ON o.customer_id = c.cus_id
            WHERE o.order_type = 'SALE'
              AND (? = 0 OR o.branch_id = ?)
              AND (? = 0 OR o.emp_id = ?)
            ORDER BY o.order_id DESC
            """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, branchId);
            ps.setInt(3, branchId);
            ps.setInt(4, empId);
            ps.setInt(5, empId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("orderId", rs.getInt("order_id"));
                    row.put("orderCode", rs.getString("order_code"));
                    row.put("customerName", rs.getString("customerName"));
                    row.put("paymentMethod", rs.getString("payment_method"));
                    row.put("totalAmount", rs.getDouble("total_amount"));
                    row.put("status", rs.getString("status"));
                    row.put("totalItems", rs.getInt("total_items"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        row.put("createdAt", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ts));
                    } else {
                        row.put("createdAt", "");
                    }
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
