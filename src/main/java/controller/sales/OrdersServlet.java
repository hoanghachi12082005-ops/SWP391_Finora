package controller.sales;

import dao.sales.OrderDAO;
import dao.system.AuditLogDAO;
import model.Order;
import model.OrderDetail;
import model.AuditLog;
import model.Employee;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(urlPatterns = {"/orders", "/orders/detail", "/orders/refund"})
public class OrdersServlet extends HttpServlet {

    private final OrderDAO orderDao = new OrderDAO();
    private final AuditLogDAO auditLogDao = new AuditLogDAO();

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

        // Lấy tất cả đơn hàng thuộc chi nhánh của nhân viên (hoặc tất cả chi nhánh nếu cần, 
        // nhưng để lọc theo branch của emp đang làm việc là chuẩn nghiệp vụ).
        // Cho admin hoặc manager, họ xem toàn bộ nếu branchId = 0.
        // Ở đây ta lọc theo branchId của nhân viên đang đăng nhập.
        int branchId = emp.getBranchId();

        List<Order> orders = orderDao.getAllSaleOrders(keyword, branchId);
        req.setAttribute("orders", orders);
        req.setAttribute("keyword", keyword);

        req.getRequestDispatcher("/WEB-INF/views/orders.jsp").forward(req, resp);
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
        json.append("\"employeeName\":\"").append(escJson(order.getEmployeeName())).append("\",");
        json.append("\"branchName\":\"").append(escJson(order.getBranchName())).append("\",");
        json.append("\"subtotal\":").append(order.getSubtotal()).append(",");
        json.append("\"discountAmount\":").append(order.getDiscountAmount()).append(",");
        json.append("\"totalAmount\":").append(order.getTotalAmount()).append(",");
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

        if (order.getStatus() == Order.OrderStatus.COMPLETED) {
        } else {
            out.write("{\"status\":\"error\",\"message\":\"Chỉ có thể hoàn trả đơn hàng đã hoàn thành.\"}");
            return;
        }

        // Update status to CANCELLED
        boolean success = orderDao.updateStatus(orderId, "CANCELLED");
        if (success) {
            // Log to audit_log
            AuditLog log = new AuditLog();
            log.setEmpId(emp.getEmpId());
            log.setActionName("REFUND");
            log.setTableName("order");
            log.setRecordId(orderId);
            log.setOldData("status=COMPLETED");
            log.setNewData("status=CANCELLED");
            auditLogDao.insert(log);

            out.write("{\"status\":\"success\",\"message\":\"Hoàn trả đơn hàng thành công.\"}");
        } else {
            out.write("{\"status\":\"error\",\"message\":\"Không thể cập nhật trạng thái đơn hàng.\"}");
        }
    }

    private String escJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
