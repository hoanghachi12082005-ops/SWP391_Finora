package controller.vnpay;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.vnpay.VNPayService;

import java.io.IOException;
import java.util.Map;

/**
 * Hiển thị kết quả thanh toán VNPay với URL sạch.
 * Nhận redirect từ VNPayReturnServlet, decode+verify token, forward sang JSP.
 * GET /vnpay/result
 */
@WebServlet("/vnpay/result")
public class VNPayResultServlet extends HttpServlet {

    private final VNPayService vnpay = new VNPayService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String tokenParam = req.getParameter("t");
        if (tokenParam == null || tokenParam.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing result token");
            return;
        }

        // Giải mã + verify HMAC trong 1 bước
        Map<String, String> data = vnpay.decodeResultToken(tokenParam);
        if (data == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid or tampered result token");
            return;
        }

        // Set attributes cho JSP
        req.setAttribute("status", data.get("status"));
        req.setAttribute("message", data.getOrDefault("message", "Không có thông tin"));
        req.setAttribute("orderCode", data.get("orderCode"));
        String amount = data.get("amount");
        req.setAttribute("amount", amount != null && !amount.isEmpty() ? Long.parseLong(amount) : 0);
        req.setAttribute("transactionNo", data.get("transactionNo"));
        req.setAttribute("bankCode", data.get("bankCode"));
        req.setAttribute("payDate", data.get("payDate"));

        req.getRequestDispatcher("/views/common/vnpay_result.jsp").forward(req, resp);
    }
}
