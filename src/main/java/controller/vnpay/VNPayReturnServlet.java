package controller.vnpay;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

        if (isSuccess && orderCode != null) {
            long amount = amountStr != null ? Long.parseLong(amountStr) : 0;
            vnpay.processSuccess(orderCode, transactionNo, amount, bankCode);
        } else if (!isSuccess && orderCode != null) {
            vnpay.processFailed(orderCode, responseCode);
        }

        // Redirect — chỉ truyền orderCode (không lộ thông tin nhạy cảm)
        if (isSuccess && orderCode != null) {
            resp.sendRedirect(req.getContextPath() + "/payment/process?orderCode=" + orderCode);
        } else if (orderCode != null) {
            resp.sendRedirect(req.getContextPath() + "/payment/failed?orderCode=" + orderCode);
        } else {
            resp.sendRedirect(req.getContextPath() + "/payment/failed");
        }
    }
}
