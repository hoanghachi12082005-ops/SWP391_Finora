package controller.vnpay;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet({"/payment/process", "/payment/failed"})
public class VNPayResultServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        String orderCode = req.getParameter("orderCode");
        String amountStr = req.getParameter("amount");

        long amount = 0;
        if (amountStr != null && !amountStr.isEmpty()) {
            try { amount = Long.parseLong(amountStr); } catch (NumberFormatException ignored) {}
        }

        req.setAttribute("status", "/payment/process".equals(path) ? "success" : "failed");
        req.setAttribute("message", "/payment/process".equals(path) ? "Giao dịch thành công!" : "Giao dịch thất bại!");
        req.setAttribute("orderCode", orderCode != null ? orderCode : "");
        req.setAttribute("amount", amount);

        req.getRequestDispatcher("/views/common/vnpay_result.jsp").forward(req, resp);
    }
}
