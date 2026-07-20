package controller.vnpay;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.vnpay.VNPayService;

import java.io.IOException;
import java.util.Map;

@WebServlet("/vnpay/return")
public class VNPayReturnServlet extends HttpServlet {

    private final VNPayService vnpay = new VNPayService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> params = vnpay.extractParams(req);
        boolean isValid = vnpay.verifySignature(params);

        String orderCode   = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transStatus  = params.get("vnp_TransactionStatus");
        String transactionNo = params.get("vnp_TransactionNo");
        String amountStr   = params.get("vnp_Amount");
        String bankCode    = params.get("vnp_BankCode");

        boolean isSuccess = isValid && "00".equals(responseCode)
                && ("00".equals(transStatus) || transStatus == null);

        long amount = 0;
        if (isSuccess && orderCode != null) {
            amount = amountStr != null ? Long.parseLong(amountStr) : 0;
            vnpay.processSuccess(orderCode, transactionNo, amount, bankCode);
        } else if (!isSuccess && orderCode != null) {
            vnpay.processFailed(orderCode, responseCode);
        }

        // Lưu kết quả vào session
        HttpSession session = req.getSession();
        session.setAttribute("paymentStatus", isSuccess ? "success" : "failed");
        session.setAttribute("paymentOrderCode", orderCode);
        session.setAttribute("paymentAmount", amount / 100);
        session.setAttribute("paymentTransactionNo", transactionNo);
        session.setAttribute("paymentBankCode", bankCode);

        // Redirect — URL sạch
        resp.sendRedirect(req.getContextPath() + (isSuccess ? "/payment/success" : "/payment/failed"));
    }
}
