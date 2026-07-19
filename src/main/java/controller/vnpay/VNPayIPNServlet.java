package controller.vnpay;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.vnpay.VNPayService;
import util.database.DBContext;

import java.io.IOException;
import java.sql.Connection;
import java.util.Map;

/**
 * IPN (Instant Payment Notification) - callback server-to-server từ VNPay.
 * Chỉ hoạt động khi deploy lên host thật (không chạy được trên localhost).
 * GET /vnpay/ipn
 */
@WebServlet("/vnpay/ipn")
public class VNPayIPNServlet extends HttpServlet {

    private final VNPayService vnpay = new VNPayService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, String> params = vnpay.extractParams(req);
        if (!vnpay.verifySignature(params)) {
            writeJson(resp, "97", "Invalid signature");
            return;
        }

        String orderCode     = params.get("vnp_TxnRef");
        String responseCode  = params.get("vnp_ResponseCode");
        String transStatus   = params.get("vnp_TransactionStatus");
        String amountStr     = params.get("vnp_Amount");
        String transactionNo = params.get("vnp_TransactionNo");
        String bankCode      = params.get("vnp_BankCode");

        boolean isPaid = "00".equals(responseCode) && "00".equals(transStatus);

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (isPaid) {
                    vnpay.processSuccessInTransaction(conn, orderCode, transactionNo,
                            Long.parseLong(amountStr), bankCode);
                } else {
                    vnpay.processFailedInTransaction(conn, orderCode, responseCode);
                }
                conn.commit();
                writeJson(resp, "00", "Confirm Success");
            } catch (Exception e) {
                conn.rollback();
                writeJson(resp, "01", e.getMessage());
            }
        } catch (Exception e) {
            writeJson(resp, "99", "Database error");
        }
    }

    private void writeJson(HttpServletResponse resp, String code, String message) throws IOException {
        resp.getWriter().print("{\"RspCode\":\"" + code + "\",\"Message\":\"" + message + "\"}");
    }
}
