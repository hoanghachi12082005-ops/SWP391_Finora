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

@WebServlet({"/payment/process", "/payment/failed"})
public class VNPayResultServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        String orderCode = req.getParameter("orderCode");

        if ("/payment/process".equals(path)) {
            req.setAttribute("status", "success");
            req.setAttribute("message", "Giao dịch thành công!");

            // Query thông tin đơn hàng từ DB
            if (orderCode != null) {
                req.setAttribute("orderCode", orderCode);
                try (Connection conn = DBContext.getConnection()) {
                    Order order = new OrderDAO().findByCode(conn, orderCode);
                    req.setAttribute("amount", order != null ? (long) order.getTotalAmount() : 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    req.setAttribute("amount", 0);
                }
            }
        } else {
            req.setAttribute("status", "failed");
            req.setAttribute("message", "Giao dịch thất bại!");
            req.setAttribute("orderCode", orderCode != null ? orderCode : "");
            req.setAttribute("amount", 0);
        }

        req.getRequestDispatcher("/views/common/vnpay_result.jsp").forward(req, resp);
    }
}
