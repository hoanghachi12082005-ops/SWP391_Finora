package service.sales;

import dao.customer.CustomerDAO;
import dao.finance.PaymentDAO;
import dao.inventory.InventoryItemDAO;
import dao.inventory.StockTransactionDAO;
import dao.sales.OrderDAO;
import dao.sales.OrderDetailDAO;
import dao.system.ActivityLogDAO;
import model.Order;
import model.OrderDetail;
import model.Payment;
import util.database.DBContext;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderService {
    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderDetailDAO orderDetailDAO = new OrderDetailDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final InventoryItemDAO inventoryDAO = new InventoryItemDAO();
    private final StockTransactionDAO stockTxDAO = new StockTransactionDAO();
    private final ActivityLogDAO auditDAO = new ActivityLogDAO();

    public List<Order> findAll() { return orderDAO.findAll(); }
    public Order findById(int id) { return orderDAO.findById(id); }

    public int checkout(Order order, List<OrderDetail> details, int earnedPoints, Integer empId) throws SQLException {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String orderCode = generateOrderCode();
                order.setOrderCode(orderCode);
                order.setStatus("COMPLETED");

                int orderId = orderDAO.insert(order, conn);

                orderDetailDAO.insertBatch(orderId, details, conn);

                Payment payment = new Payment();
                payment.setOrderId(orderId);
                payment.setPaymentAmount(order.getTotalAmount());
                payment.setPaymentStatus("PAID");
                payment.setTransactionCode("TXN-" + orderCode);
                paymentDAO.insert(payment, conn);

                int warehouseId = resolveWarehouseId(order.getBranchId(), conn);
                for (OrderDetail d : details) {
                    inventoryDAO.deductStock(d.getProductId(), warehouseId, d.getQuantity(), conn);

                    // find before_qty
                    int beforeQty = 0; // minimal: deductStock already checks
                    stockTxDAO.insert(warehouseId, d.getProductId(), "ORDER", orderId, "SALE_DEDUCT",
                            d.getQuantity(), 0, 0, empId, conn);
                }

                if (order.getCustomerId() != null && order.getCustomerId() > 0 && earnedPoints > 0) {
                    customerDAO.earnPoints(conn, order.getCustomerId(), earnedPoints, orderId);
                }

                if (order.getCustomerId() != null && order.getCustomerId() > 0) {
                    BigDecimal spent = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
                    String updateSpentSql = "UPDATE customer SET total_spent = COALESCE(total_spent, 0) + ?, updated_at = GETDATE() WHERE cus_id = ?";
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(updateSpentSql)) {
                        ps.setBigDecimal(1, spent);
                        ps.setInt(2, order.getCustomerId());
                        ps.executeUpdate();
                    }
                }

                if (empId != null) {
                    auditDAO.log(empId, "CREATE", "Order", orderId, null, "POS checkout: " + orderCode);
                }

                conn.commit();
                return orderId;
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException) throw (SQLException) ex;
                throw new SQLException("Checkout failed", ex);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private String generateOrderCode() {
        return "ORD" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + System.currentTimeMillis() % 100000;
    }

    private int resolveWarehouseId(Integer branchId, Connection conn) throws SQLException {
        if (branchId == null) throw new SQLException("Branch ID required for checkout");
        String sql = "SELECT TOP 1 warehouse_id FROM warehouse WHERE branch_id = ? AND status = 'ACTIVE'";
        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("warehouse_id");
            }
        }
        throw new SQLException("No active warehouse found for branch " + branchId);
    }
}
