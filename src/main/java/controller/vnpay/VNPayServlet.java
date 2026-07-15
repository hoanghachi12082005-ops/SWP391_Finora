package controller.vnpay;

import dao.sales.OrderDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.vnpay.VNPayService;
import util.database.DBContext;
import util.vnpay.Config;

import java.io.IOException;
import java.sql.Connection;

/**
 * Tạo URL thanh toán VNPay và redirect sang cổng thanh toán.
 * GET /vnpay/pay?orderCode=HD...
 */
@WebServlet("/vnpay/pay")
public class VNPayServlet extends HttpServlet {

    private final VNPayService vnpay = new VNPayService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String orderCode = req.getParameter("orderCode");
        if (orderCode == null || orderCode.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/views/sales/sales.jsp?error=missing_order_code");
            return;
        }

        // Lấy số tiền từ DB
        long totalAmount;
        try (Connection conn = DBContext.getConnection()) {
            OrderDAO orderDAO = new OrderDAO();
            var order = orderDAO.findByCode(conn, orderCode);
            if (order == null) {
                resp.sendRedirect(req.getContextPath() + "/views/sales/sales.jsp?error=order_not_found");
                return;
            }
            totalAmount = (long) order.getTotalAmount();
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/views/sales/sales.jsp?error=db_error");
            return;
        }

        // Tạo URL thanh toán
        String ipAddr = Config.getIpAddress(req);
        String returnUrl = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort()
                + req.getContextPath() + "/vnpay/return";
        String paymentUrl = vnpay.buildPaymentUrl(orderCode, totalAmount, ipAddr, returnUrl);

        resp.sendRedirect(paymentUrl);
    }
}
