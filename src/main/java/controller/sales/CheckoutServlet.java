package controller.sales;

import dao.sales.*;
import dao.inventory.InventoryDAO;
import dao.finance.PaymentDAO;
import model.*;
import util.database.DBContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Xử lý thanh toán POS — thực hiện toàn bộ logic trong 1 TRANSACTION. POST
 * /checkout
 */
@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    private static final String CART_ATTR = "cart";
    private static final String ACTIVE_TAB_ATTR = "activeTab";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession();
        Employee emp = (Employee) session.getAttribute("employee");
        if (emp == null) {
            out.write("{\"status\":\"error\",\"message\":\"Chưa đăng nhập. Vui lòng đăng nhập lại.\"}");
            return;
        }

        // Nhận tabId cần thanh toán
        String tabIdStr = req.getParameter("tabId");
        int tabId = 1;
        try {
            if (tabIdStr != null && !tabIdStr.isBlank()) {
                tabId = Integer.parseInt(tabIdStr);
            }
        } catch (NumberFormatException ignored) {
        }

        @SuppressWarnings("unchecked")
        Map<Integer, OrderTab> tabs = (Map<Integer, OrderTab>) session.getAttribute("cartTabs");
        if (tabs == null || !tabs.containsKey(tabId)) {
            out.write("{\"status\":\"error\",\"message\":\"Không tìm thấy đơn hàng tương ứng.\"}");
            return;
        }

        OrderTab tab = tabs.get(tabId);
        List<CartItem> cart = tab.getItems();
        if (cart == null || cart.isEmpty()) {
            out.write("{\"status\":\"error\",\"message\":\"Giỏ hàng trống. Vui lòng thêm sản phẩm.\"}");
            return;
        }

        // Parse parameters
        String paymentMethod = req.getParameter("paymentMethod");
        if (paymentMethod == null || paymentMethod.isBlank()) {
            paymentMethod = "CASH";
        }
        if (!paymentMethod.equals("CASH") && !paymentMethod.equals("BANK_TRANSFER")) {
            paymentMethod = "CASH";
        }


//session.getAttribute
        String cashReceivedStr = req.getParameter("cashReceived");
        double cashReceived = 0;
        if (cashReceivedStr != null && !cashReceivedStr.isBlank()) {
            try {
                cashReceived = Double.parseDouble(cashReceivedStr);
            } catch (NumberFormatException ignored) {
            }
        }

        int warehouseId = getWarehouseId(emp.getBranchId());
        if (warehouseId <= 0) {
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Kho hàng của cửa hàng chưa được tạo. Vui lòng liên hệ Quản lý cửa hàng hoặc Chủ cửa hàng.\"}");
            return;
        }

        // ── Tính toán tổng ──────────────────────────────────────
        double subtotal = tab.getSubtotal();
        double redeemDiscount = tab.getRedeemDiscount();
        int redeemPoints = tab.getRedeemPoints();
        Customer selectedCustomer = tab.getSelectedCustomer();
        Integer customerId = selectedCustomer != null ? selectedCustomer.getCusId() : null;

        double totalDiscount = redeemDiscount;
        double totalBeforeTax = subtotal - totalDiscount;
        double vat = tab.getVatAmount(); // VAT tính theo từng ngành hàng
        double totalAmount = totalBeforeTax + vat;

        // ══════════════════════════════════════════════════════════
        // Chuyển khoản: tạo đơn hàng chờ → VNPAY QR
        // ══════════════════════════════════════════════════════════
        if ("BANK_TRANSFER".equals(paymentMethod)) {

            Connection conn = null;
            try {
                conn = DBContext.getConnection();
                conn.setAutoCommit(false);

                OrderDAO orderDao = new OrderDAO();
                OrderDetailDAO detailDao = new OrderDetailDAO();

                String orderCode = "HD" + System.currentTimeMillis();
                Order order = new Order();
                order.setOrderCode(orderCode);
                order.setOrderType("SALE");
                order.setCustomerId(customerId);
                order.setBranchId(emp.getBranchId());
                order.setEmpId(emp.getEmpId());
                order.setWarehouseId(warehouseId);
                order.setSubtotal(subtotal);
                order.setDiscountAmount(totalDiscount);
                order.setTotalAmount(totalAmount);
                order.setPaymentMethod("BANK_TRANSFER");
                order.setStatus(Order.OrderStatus.PENDING);

                int orderId = orderDao.createOrderInTransaction(conn, order);
                detailDao.insertBatch(conn, orderId, cart);

                InventoryDAO inventoryDao = new InventoryDAO();
                for (CartItem item : cart) {
                    int beforeQty = inventoryDao.getStockInTransaction(conn, item.getProductId(), warehouseId);
                    int deducted = inventoryDao.deductStock(conn, item.getProductId(), warehouseId, item.getQuantity());
                    if (deducted == 0) {
                        throw new SQLException("Không trừ được tồn kho sản phẩm: " + item.getProductName());
                    }
                    inventoryDao.logStockTransaction(conn, warehouseId, item.getProductId(),
                            orderId, item.getQuantity(), beforeQty, emp.getEmpId());
                }

                conn.commit();

                tabs.remove(tabId);
                if (tabs.isEmpty()) {
                    tabs.put(1, new OrderTab(1));
                }

                String vnpayUrl = req.getContextPath() + "/vnpay/pay";
                out.write("{\"status\":\"vnpay\",\"vnpayUrl\":\"" + vnpayUrl + "\",\"orderCode\":\"" + orderCode + "\"}");

            } catch (SQLException e) {
                e.printStackTrace();
                if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
                out.write("{\"status\":\"error\",\"message\":\"Lỗi hệ thống: " + escJson(e.getMessage()) + "\"}");
            } finally {
                if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {} }
            }
            return;
        }

        System.out.println("[DEBUG] CASH flow — KHÔNG có session lock, chỉ dùng frontend disable button");
        if ("CASH".equals(paymentMethod) && cashReceived < totalAmount) {
            out.write("{\"status\":\"error\",\"message\":\"Số tiền khách thanh toán không đủ. Cần: "
                    + String.format("%.0f", totalAmount) + " VND\"}");
            return;
        }

        double changeAmount = "CASH".equals(paymentMethod) ? cashReceived - totalAmount : 0;

        // ── TRANSACTION ─────────────────────────────────────────
        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            InventoryDAO inventoryDao = new InventoryDAO();
            OrderDAO orderDao = new OrderDAO();
            OrderDetailDAO detailDao = new OrderDetailDAO();
            PaymentDAO paymentDao = new PaymentDAO();
            CustomerPointDAO pointDao = new CustomerPointDAO();
            // 1. Kiểm tra tồn kho từng sản phẩm
            for (CartItem item : cart) {
                int stock = inventoryDao.getStockInTransaction(conn, item.getProductId(), warehouseId);
                if (stock < item.getQuantity()) {
                    conn.rollback();
                    out.write("{\"status\":\"error\",\"message\":\"Sản phẩm \\\"" + escJson(item.getProductName())
                            + "\\\" không đủ tồn kho (cần " + item.getQuantity() + ", còn " + stock + ").\"}");
                    return;
                }
            }

            // 2. Tạo đơn hàng với discountAmount = redeemDiscount
            String orderCode = "HD" + System.currentTimeMillis();
            Order order = new Order();
            order.setOrderCode(orderCode);
            order.setOrderType("SALE");
            order.setCustomerId(customerId);
            order.setBranchId(emp.getBranchId());
            order.setEmpId(emp.getEmpId());
            order.setWarehouseId(warehouseId);
            order.setSubtotal(subtotal);
            order.setDiscountAmount(totalDiscount);
            order.setTotalAmount(totalAmount);
            order.setPaymentMethod(paymentMethod);
            order.setStatus(Order.OrderStatus.PENDING);

            int orderId = orderDao.createOrderInTransaction(conn, order);

            // 3. Chèn chi tiết đơn hàng
            detailDao.insertBatch(conn, orderId, cart);

            // 4. Tạo payment record (dựa trên totalAmount đã trừ redeem discount)
            Payment payment = new Payment();
            payment.setOrderId(orderId);
            payment.setPaymentAmount(totalAmount);
            payment.setPaymentStatus("PAID");
            payment.setTransactionCode(paymentMethod.equals("CASH") ? "CASH-" + orderId : "BANK-" + orderId);
            payment.setPaymentType("INCOME");
            payment.setMethod(paymentMethod);
            payment.setDescription("Thanh toán đơn hàng " + orderCode);
            payment.setEmployeeId(emp.getEmpId());
            payment.setBranchId(emp.getBranchId());
            paymentDao.insert(conn, payment);

            // 5. Trừ kho + log stock_transaction
            for (CartItem item : cart) {
                int beforeQty = inventoryDao.getStockInTransaction(conn, item.getProductId(), warehouseId);
                int deducted = inventoryDao.deductStock(conn, item.getProductId(), warehouseId, item.getQuantity());
                if (deducted == 0) {
                    throw new SQLException("Không trừ được tồn kho sản phẩm: " + item.getProductName());
                }
                inventoryDao.logStockTransaction(conn, warehouseId, item.getProductId(),
                        orderId, item.getQuantity(), beforeQty, emp.getEmpId());
            }

            // 6. Trừ điểm đã đổi (chỉ sau khi payment thành công)
            if (customerId != null && customerId > 0 && redeemPoints > 0) {
                int available = pointDao.getCurrentPoints(customerId);
                if (available < redeemPoints) {
                    throw new SQLException("Không đủ điểm tích lũy.");
                }
                pointDao.deductPoints(conn, customerId, redeemPoints, orderId);
            }

            // 8. Tích điểm cho khách hàng dựa trên số tiền thực tế đã thanh toán (totalAmount)
            if (customerId != null && customerId > 0) {
                pointDao.addPoints(conn, customerId, totalAmount, orderId);
            }

            // 9. Cập nhật trạng thái → COMPLETED
            orderDao.updateStatus(conn, orderId, "COMPLETED");

            // COMMIT
            conn.commit();

            // Xóa tab vừa thanh toán
            tabs.remove(tabId);
            if (tabs.isEmpty()) {
                tabs.put(1, new OrderTab(1));
                session.setAttribute(ACTIVE_TAB_ATTR, 1);
            } else {
                // Chuyển active tab sang tab còn lại đầu tiên
                int remainingActiveTabId = tabs.keySet().iterator().next();
                session.setAttribute(ACTIVE_TAB_ATTR, remainingActiveTabId);
            }

// Lưu lại cartTabs vào session
            session.setAttribute("cartTabs", tabs);

            // Trả kết quả thành công
            out.write("{\"status\":\"success\",");
            out.write("\"orderCode\":\"" + escJson(orderCode) + "\",");
            out.write("\"subtotal\":" + subtotal + ",");
            out.write("\"redeemDiscount\":" + redeemDiscount + ",");
            out.write("\"redeemPoints\":" + redeemPoints + ",");
            out.write("\"vat\":" + vat + ",");
            out.write("\"totalAmount\":" + totalAmount + ",");
            out.write("\"cashReceived\":" + cashReceived + ",");
            out.write("\"changeAmount\":" + changeAmount + "}");

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            out.write("{\"status\":\"error\",\"message\":\"Lỗi hệ thống khi thanh toán: " + escJson(e.getMessage()) + "\"}");
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                    
                } catch (SQLException ignored) {
                }
            }
        }
    }

    private int getWarehouseId(int branchId) {
        if (branchId <= 0) return 0;
        try (var conn = DBContext.getConnection(); var ps = conn.prepareStatement("SELECT TOP 1 warehouse_id FROM warehouse WHERE branch_id = ? AND status = 'ACTIVE'")) {
            ps.setInt(1, branchId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("warehouse_id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String escJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
