package controller.vnpay;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet({"/payment/success", "/payment/failed"})
public class VNPayResultServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();

        String status = (String) session.getAttribute("paymentStatus");
        String orderCode = (String) session.getAttribute("paymentOrderCode");
        Long amount = (Long) session.getAttribute("paymentAmount");
        String transactionNo = (String) session.getAttribute("paymentTransactionNo");
        String bankCode = (String) session.getAttribute("paymentBankCode");

        // Xóa session để F5 không dùng lại dữ liệu cũ
        session.removeAttribute("paymentStatus");
        session.removeAttribute("paymentOrderCode");
        session.removeAttribute("paymentAmount");
        session.removeAttribute("paymentTransactionNo");
        session.removeAttribute("paymentBankCode");

        req.setAttribute("status", status != null ? status : "failed");
        req.setAttribute("orderCode", orderCode != null ? orderCode : "");
        req.setAttribute("amount", amount != null ? amount : 0L);
        req.setAttribute("transactionNo", transactionNo != null ? transactionNo : "");
        req.setAttribute("bankCode", bankCode != null ? bankCode : "");

        if ("success".equals(status)) {
            req.setAttribute("message", "Giao dịch thành công!");
        } else {
            req.setAttribute("message", "Giao dịch thất bại!");
        }

        req.getRequestDispatcher("/views/common/vnpay_result.jsp").forward(req, resp);
    }
}
