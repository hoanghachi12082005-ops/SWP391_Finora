package controller.sales;

import dao.sales.OrderDAO;
import util.database.DBContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;

/**
 * Kiểm tra trạng thái đơn hàng (dùng cho polling VNPAY).
 * GET /order/status?orderCode=HD...
 */
@WebServlet("/order/status")
public class OrderStatusServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String orderCode = req.getParameter("orderCode");
        if (orderCode == null || orderCode.isBlank()) {
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Thiếu mã đơn hàng\"}");
            return;
        }

        try (Connection conn = DBContext.getConnection()) {
            OrderDAO orderDAO = new OrderDAO();
            String orderStatus = orderDAO.getStatusByCode(conn, orderCode);
            if (orderStatus == null) {
                resp.getWriter().write("{\"status\":\"not_found\"}");
                return;
            }
            resp.getWriter().write("{\"status\":\"" + orderStatus + "\"}");
        } catch (Exception e) {
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}
