package controller.sales;

import dao.sales.OrderDAO;
import dao.inventory.InventoryDAO;
import dao.sales.CustomerPointDAO;
import dao.system.VatSettingDAO;
import model.Order;
import model.OrderDetail;
import model.Employee;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import util.database.DBContext;

@WebServlet(urlPatterns = {"/orders", "/orders/detail"})
public class OrdersServlet extends HttpServlet {

    private final OrderDAO orderDao = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getServletPath();

        if ("/orders/detail".equals(path)) {
            handleOrderDetail(req, resp);
            return;
        }

        // Default: Lịch sử đơn hàng
        req.setAttribute("activePage", "orders");

        HttpSession session = req.getSession();
        Employee emp = (Employee) session.getAttribute("employee");
        if (emp == null) {
            emp = new Employee();
            emp.setEmpId(1);
            emp.setBranchId(1);
            emp.setFullName("Thu ngân #1");
            session.setAttribute("employee", emp);
        }

        String keyword = req.getParameter("keyword");
        if (keyword != null) {
            keyword = keyword.trim();
        }

        // Filter params: status, payment method, date range
        String status = req.getParameter("status");
        if (status != null) status = status.trim();
        String paymentMethod = req.getParameter("paymentMethod");
        if (paymentMethod != null) paymentMethod = paymentMethod.trim();
        String dateFrom = req.getParameter("dateFrom");
        if (dateFrom != null) dateFrom = dateFrom.trim();
        String dateTo = req.getParameter("dateTo");
        if (dateTo != null) dateTo = dateTo.trim();

        // Lấy tất cả đơn hàng thuộc chi nhánh của nhân viên
        int branchId = emp.getBranchId();

        // Xác định quyền xem: SalesStaff chỉ thấy đơn của mình; Owner/StoreManager/Admin thấy tất cả
        String roleName = emp.getRoleName() != null ? emp.getRoleName().trim().toLowerCase() : "";
        boolean isSalesStaff = !roleName.equals("owner") && !roleName.equals("storemanager") && !roleName.equals("admin");
        int filterEmpId = isSalesStaff ? emp.getEmpId() : 0;

        int page = 1;
        String pageStr = req.getParameter("page");
        if (pageStr != null) {
            try {
                page = Integer.parseInt(pageStr);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        int sizeValue = 10;
        String sizeStr = req.getParameter("sizeValue");
        if (sizeStr != null) {
            try {
                sizeValue = Integer.parseInt(sizeStr);
                if (sizeValue < 1) sizeValue = 10;
            } catch (NumberFormatException e) {
                sizeValue = 10;
            }
        }

        int totalOrders = orderDao.countSaleOrders(keyword, branchId, filterEmpId, status, paymentMethod, dateFrom, dateTo);

        int pageSize = sizeValue;
        if (sizeValue == 100) {
            pageSize = totalOrders > 0 ? totalOrders : 10;
        }

        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);
        if (totalPages < 1) totalPages = 1;
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * pageSize;
        List<Order> orders = orderDao.getAllSaleOrdersPaginated(keyword, branchId, filterEmpId, offset, pageSize,
                                                                  status, paymentMethod, dateFrom, dateTo);

        int startRecord = totalOrders == 0 ? 0 : offset + 1;
        int endRecord = Math.min(page * pageSize, totalOrders);

        req.setAttribute("orders", orders);
        req.setAttribute("keyword", keyword);
        req.setAttribute("selectedStatus", status);
        req.setAttribute("selectedPayment", paymentMethod);
        req.setAttribute("dateFrom", dateFrom);
        req.setAttribute("dateTo", dateTo);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("sizeValue", sizeValue);
        req.setAttribute("totalOrders", totalOrders);
        req.setAttribute("startRecord", startRecord);
        req.setAttribute("endRecord", endRecord);

        // VAT percentage cho hiển thị
        double vatPercentage = VatSettingDAO.getVatPercentage();
        req.setAttribute("vatPercentage", vatPercentage);

        req.getRequestDispatcher("/views/sales/orders.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getServletPath();

        if ("/orders/refund".equals(path)) {
            handleOrderRefund(req, resp);
        }
    }

    private void handleOrderDetail(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String idStr = req.getParameter("id");
        int orderId = 0;
        try {
            orderId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            out.write("{\"error\":\"Mã đơn hàng không hợp lệ.\"}");
            return;
        }

        Order order = orderDao.findById(orderId);
        if (order == null) {
            out.write("{\"error\":\"Không tìm thấy đơn hàng.\"}");
            return;
        }

        List<OrderDetail> details = orderDao.getOrderDetailById(orderId);

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"orderId\":").append(order.getOrderId()).append(",");
        json.append("\"orderCode\":\"").append(escJson(order.getOrderCode())).append("\",");
        json.append("\"createdAt\":\"").append(escJson(order.getCreatedAt())).append("\",");
        json.append("\"customerName\":\"").append(escJson(order.getCustomerName() != null ? order.getCustomerName() : "Khách vãng lai")).append("\",");
        json.append("\"customerPhone\":\"").append(escJson(order.getCustomerPhone() != null ? order.getCustomerPhone() : "")).append("\",");
        json.append("\"customerPoints\":").append(order.getCustomerPoints() != null ? order.getCustomerPoints() : 0).append(",");
        json.append("\"employeeName\":\"").append(escJson(order.getEmployeeName())).append("\",");
        json.append("\"branchName\":\"").append(escJson(order.getBranchName())).append("\",");
        json.append("\"subtotal\":").append(order.getSubtotal()).append(",");
        json.append("\"discountAmount\":").append(order.getDiscountAmount()).append(",");
        json.append("\"totalAmount\":").append(order.getTotalAmount()).append(",");
        // Tính VAT thực tế từ tổng tiền: VAT = totalAmount - (subtotal - discount)
        double actualVat = order.getTotalAmount() - (order.getSubtotal() - order.getDiscountAmount());
        if (actualVat < 0) actualVat = 0;
        json.append("\"vatAmount\":").append(actualVat).append(",");
        json.append("\"vatPercentage\":").append(VatSettingDAO.getVatPercentage()).append(",");
        json.append("\"paymentMethod\":\"").append(escJson(order.getPaymentMethod())).append("\",");
        json.append("\"status\":\"").append(escJson(order.getStatus().name())).append("\",");
        json.append("\"items\":[");
        for (int i = 0; i < details.size(); i++) {
            OrderDetail d = details.get(i);
            json.append("{");
            json.append("\"productName\":\"").append(escJson(d.getProductName())).append("\",");
            json.append("\"productCode\":\"").append(escJson(d.getProductCode())).append("\",");
            json.append("\"quantity\":").append(d.getQuantity()).append(",");
            json.append("\"unitPrice\":").append(d.getUnitPrice()).append(",");
            json.append("\"totalPrice\":").append(d.getTotalPrice());
            json.append("}");
            if (i < details.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        json.append("}");

        out.write(json.toString());
    }

    private void handleOrderRefund(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession();
        Employee emp = (Employee) session.getAttribute("employee");
        if (emp == null) {
            out.write("{\"status\":\"error\",\"message\":\"Chưa đăng nhập.\"}");
            return;
        }

        String idStr = req.getParameter("orderId");
        int orderId = 0;
        try {
            orderId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            out.write("{\"status\":\"error\",\"message\":\"Mã đơn hàng không hợp lệ.\"}");
            return;
        }

        Order order = orderDao.findById(orderId);
        if (order == null) {
            out.write("{\"status\":\"error\",\"message\":\"Không tìm thấy đơn hàng.\"}");
            return;
        }

        if (order.getStatus() != Order.OrderStatus.COMPLETED) {
            out.write("{\"status\":\"error\",\"message\":\"Chỉ có thể hoàn trả đơn hàng đã hoàn thành.\"}");
            return;
        }

        List<OrderDetail> details = orderDao.getOrderDetailById(orderId);
        InventoryDAO inventoryDao = new InventoryDAO();
        CustomerPointDAO pointDao = new CustomerPointDAO();

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            // 1. Hoàn lại tồn kho
            for (OrderDetail d : details) {
                int beforeQty = inventoryDao.getStockInTransaction(conn, d.getProductId(), order.getWarehouseId());
                String restoreSql = "UPDATE inventory SET quantity_in_stock = quantity_in_stock + ?, updated_at = GETDATE() WHERE warehouse_id = ? AND product_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(restoreSql)) {
                    ps.setInt(1, d.getQuantity());
                    ps.setInt(2, order.getWarehouseId());
                    ps.setInt(3, d.getProductId());
                    ps.executeUpdate();
                }
                String txSql = "INSERT INTO stock_transaction (warehouse_id, product_id, reference_type, reference_id, transaction_type, quantity, before_quantity, after_quantity, note, created_by, created_at) VALUES (?, ?, 'ORDER', ?, 'REFUND', ?, ?, ?, N'Hoàn trả đơn hàng', ?, GETDATE())";
                try (PreparedStatement ps = conn.prepareStatement(txSql)) {
                    ps.setInt(1, order.getWarehouseId());
                    ps.setInt(2, d.getProductId());
                    ps.setInt(3, orderId);
                    ps.setInt(4, d.getQuantity());
                    ps.setInt(5, beforeQty);
                    ps.setInt(6, beforeQty + d.getQuantity());
                    ps.setInt(7, emp.getEmpId());
                    ps.executeUpdate();
                }
            }

            // 2. Hoàn điểm cho khách hàng
            if (order.getCustomerId() != null && order.getCustomerId() > 0) {
                double pointsEarned = Math.round(order.getTotalAmount() / CustomerPointDAO.getEarnRate() * 100.0) / 100.0;
                if (pointsEarned > 0) {
                    String selectSql = "SELECT cus_point_id, current_points FROM customer_point WHERE cus_id = ?";
                    int cusPointId = -1;
                    double beforePoints = 0;
                    try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                        ps.setInt(1, order.getCustomerId());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                cusPointId = rs.getInt("cus_point_id");
                                beforePoints = rs.getDouble("current_points");
                            }
                        }
                    }
                    if (cusPointId != -1) {
                        String updateSql = "UPDATE customer_point SET current_points = current_points - ?, updated_at = GETDATE() WHERE cus_point_id = ?";
                        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                            ps.setDouble(1, pointsEarned);
                            ps.setInt(2, cusPointId);
                            ps.executeUpdate();
                        }
                        String logSql = "INSERT INTO point_transaction (cus_point_id, order_id, before_points, after_points, description, created_at) VALUES (?, ?, ?, ?, N'Hoàn điểm hoàn trả đơn hàng', GETDATE())";
                        try (PreparedStatement ps = conn.prepareStatement(logSql)) {
                            ps.setInt(1, cusPointId);
                            ps.setInt(2, orderId);
                            ps.setDouble(3, beforePoints);
                            ps.setDouble(4, Math.max(0, beforePoints - pointsEarned));
                            ps.executeUpdate();
                        }
                    }
                }
            }

            // 3. Cập nhật trạng thái đơn hàng
            orderDao.updateStatus(conn, orderId, "CANCELLED");

            // 4. Không ghi audit log REFUND (tính năng không dùng đến)


            conn.commit();
            out.write("{\"status\":\"success\",\"message\":\"Hoàn trả đơn hàng thành công. Đã hoàn tồn kho và điểm.\"}");
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            out.write("{\"status\":\"error\",\"message\":\"Lỗi hoàn trả: " + escJson(e.getMessage()) + "\"}");
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    private String escJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
