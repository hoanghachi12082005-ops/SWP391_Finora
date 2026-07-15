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
 * Xử lý kết quả thanh toán VNPay trả về qua browser (Return URL).
 * GET /vnpay/return
 */
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
        String payDate     = params.get("vnp_PayDate");

        boolean isSuccess = isValid && "00".equals(responseCode)
                && ("00".equals(transStatus) || transStatus == null);

        if (isSuccess && orderCode != null) {
            long amount = amountStr != null ? Long.parseLong(amountStr) : 0;
            vnpay.processSuccess(orderCode, transactionNo, amount, bankCode);
        } else if (!isSuccess && orderCode != null) {
            vnpay.processFailed(orderCode, responseCode);
        }

        req.setAttribute("status", isSuccess ? "success" : "error");
        req.setAttribute("message", vnpay.getResponseMessage(responseCode));
        req.setAttribute("orderCode", orderCode);
        req.setAttribute("amount", amountStr != null ? Long.parseLong(amountStr) / 100 : 0);
        req.setAttribute("transactionNo", transactionNo);
        req.setAttribute("bankCode", bankCode);
        req.setAttribute("payDate", payDate);

        req.getRequestDispatcher("/views/common/vnpay_result.jsp").forward(req, resp);
    }
}
