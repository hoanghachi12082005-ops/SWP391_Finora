package controller.vnpay;

import dao.sales.OrderDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Order;
import util.database.DBContext;

import java.io.IOException;
import java.sql.Connection;

/**
 * Hiển thị kết quả thanh toán VNPay.
 * Nhận request từ VNPayReturnServlet (redirect), query DB lấy số tiền, forward sang JSP.
 * GET /payment/result
 */
@WebServlet("/payment/result")
public class VNPayResultServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String status = req.getParameter("status");
        String orderCode = req.getParameter("orderCode");
        String message = req.getParameter("message");
        String transactionNo = req.getParameter("transactionNo");
        String bankCode = req.getParameter("bankCode");

        if (status == null || orderCode == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }

        req.setAttribute("status", status);
        req.setAttribute("orderCode", orderCode);
        req.setAttribute("message", message);
        req.setAttribute("transactionNo", transactionNo);
        req.setAttribute("bankCode", bankCode);
        req.setAttribute("amount", 0);

        // Query số tiền từ DB
        if (orderCode != null) {
            try (Connection conn = DBContext.getConnection()) {
                Order order = new OrderDAO().findByCode(conn, orderCode);
                req.setAttribute("amount", order != null ? (long) order.getTotalAmount() : 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        req.getRequestDispatcher("/views/common/vnpay_result.jsp").forward(req, resp);
    }
}
