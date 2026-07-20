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
        String transactionNo = req.getParameter("transactionNo");
        String bankCode = req.getParameter("bankCode");

        req.setAttribute("orderCode", orderCode != null ? orderCode : "");
        long amount = 0;
        if (amountStr != null && !amountStr.isEmpty()) {
            try { amount = Long.parseLong(amountStr); } catch (NumberFormatException ignored) {}
        }
        req.setAttribute("amount", amount);
        req.setAttribute("transactionNo", transactionNo != null ? transactionNo : "");
        req.setAttribute("bankCode", bankCode != null ? bankCode : "");

        if ("/payment/process".equals(path)) {
            req.setAttribute("status", "success");
            req.setAttribute("message", "Giao dịch thành công!");
        } else {
            req.setAttribute("status", "failed");
            req.setAttribute("message", "Giao dịch thất bại!");
        }

        req.getRequestDispatcher("/views/common/vnpay_result.jsp").forward(req, resp);
    }
}
