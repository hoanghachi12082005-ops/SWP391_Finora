package controller.sales;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Employee;
import model.Order;
import model.OrderDetail;
import model.LoyaltyPointSetting;
import dao.customer.LoyaltyPointSettingDAO;
import service.sales.OrderService;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "OrderController", urlPatterns = {"/orders/list", "/orders/create", "/orders/checkout", "/orders/detail", "/orders/update", "/orders/cancel"})
public class OrderController extends HttpServlet {

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Employee currentUser = (session == null) ? null : (Employee) session.getAttribute("currentUser");

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String roleName = currentUser.getRoleName() == null ? "" : currentUser.getRoleName().trim();
        boolean isSalesStaff = "SalesStaff".equalsIgnoreCase(roleName) || roleName.toLowerCase().contains("sales");
        boolean isManagerOrHigher = "Admin".equalsIgnoreCase(roleName)
                || "Owner".equalsIgnoreCase(roleName)
                || "StoreManager".equalsIgnoreCase(roleName);

        if (!isSalesStaff && !isManagerOrHigher) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
            return;
        }

        request.setAttribute("canViewSalesOrders", isManagerOrHigher || isSalesStaff);
        request.setAttribute("isSalesStaffView", isSalesStaff);

        String path = request.getServletPath();
        switch (path) {
            case "/orders/list":
                request.getRequestDispatcher("/views/orders/list.jsp").forward(request, response);
                break;
            case "/orders/create":
                request.getRequestDispatcher("/views/orders/create.jsp").forward(request, response);
                break;
            case "/orders/detail":
                request.getRequestDispatcher("/views/orders/detail.jsp").forward(request, response);
                break;
            case "/orders/update":
                request.getRequestDispatcher("/views/orders/update.jsp").forward(request, response);
                break;
            case "/orders/cancel":
                request.getRequestDispatcher("/views/orders/cancel.jsp").forward(request, response);
                break;
            default:
                request.getRequestDispatcher("/views/orders/create.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Employee currentUser = (session == null) ? null : (Employee) session.getAttribute("currentUser");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String path = request.getServletPath();

        if ("/orders/checkout".equals(path)) {
            if (!"SalesStaff".equalsIgnoreCase(currentUser.getRoleName())
                    && !currentUser.getRoleName().toLowerCase().contains("sales")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only Sales Staff can checkout.");
                return;
            }
            handleCheckout(request, response, currentUser);
            return;
        }

        request.setAttribute("message", "Received data. Connect Service/DAO for processing.");
        doGet(request, response);
    }

    private void handleCheckout(HttpServletRequest request, HttpServletResponse response, Employee user)
            throws IOException {
        try {
            int customerId = parseInt(request.getParameter("customerId"), -1);
            int branchId = parseInt(request.getParameter("branchId"), -1);
            BigDecimal subtotal = parseBigDecimal(request.getParameter("subtotal"));
            BigDecimal discount = parseBigDecimal(request.getParameter("discountAmount"));
            BigDecimal total = parseBigDecimal(request.getParameter("totalAmount"));
            String paymentMethod = request.getParameter("paymentMethod");
            if (paymentMethod == null || paymentMethod.isBlank()) paymentMethod = "CASH";

            if (branchId <= 0) {
                sendJson(response, "{\"status\":\"error\",\"message\":\"Branch is required.\"}");
                return;
            }
            if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
                sendJson(response, "{\"status\":\"error\",\"message\":\"Invalid total amount.\"}");
                return;
            }

            Order order = new Order();
            if (customerId > 0) order.setCustomerId(customerId);
            order.setBranchId(branchId);
            order.setEmpId(user.getEmployeeID());
            order.setSubtotal(subtotal);
            order.setDiscountAmount(discount);
            order.setTotalAmount(total);
            order.setPaymentMethod(paymentMethod);

            int itemCount = parseInt(request.getParameter("itemCount"), 0);
            List<OrderDetail> details = new ArrayList<>();
            for (int i = 0; i < itemCount; i++) {
                int productId = parseInt(request.getParameter("productId_" + i), -1);
                int qty = parseInt(request.getParameter("quantity_" + i), 0);
                BigDecimal unitPrice = parseBigDecimal(request.getParameter("unitPrice_" + i));
                BigDecimal totalPrice = parseBigDecimal(request.getParameter("totalPrice_" + i));
                if (productId <= 0 || qty <= 0) continue;
                OrderDetail d = new OrderDetail();
                d.setProductId(productId);
                d.setQuantity(qty);
                d.setUnitPrice(unitPrice);
                d.setTotalPrice(totalPrice);
                details.add(d);
            }
            if (details.isEmpty()) {
                sendJson(response, "{\"status\":\"error\",\"message\":\"No items in order.\"}");
                return;
            }

            int earnedPoints = 0;
            if (customerId > 0) {
                LoyaltyPointSettingDAO settingDAO = new LoyaltyPointSettingDAO();
                LoyaltyPointSetting setting = settingDAO.getSetting();
                if (setting != null && setting.getAmountPerPoint().compareTo(BigDecimal.ZERO) > 0) {
                    earnedPoints = total.divide(setting.getAmountPerPoint(), 0, java.math.RoundingMode.DOWN).intValue();
                }
            }

            int orderId = orderService.checkout(order, details, earnedPoints, user.getEmployeeID());
            sendJson(response, "{\"status\":\"success\",\"orderId\":" + orderId + ",\"earnedPoints\":" + earnedPoints + "}");
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(response, "{\"status\":\"error\",\"message\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }

    private void sendJson(HttpServletResponse response, String json) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(json);
        response.getWriter().flush();
    }

    private int parseInt(String value, int def) {
        try { return Integer.parseInt(value); } catch (Exception e) { return def; }
    }

    private BigDecimal parseBigDecimal(String value) {
        try { return new BigDecimal(value); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}
