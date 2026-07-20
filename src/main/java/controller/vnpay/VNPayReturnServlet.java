package controller.vnpay;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.vnpay.VNPayService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Xử lý kết quả thanh toán VNPay trả về qua browser (Return URL).
 * Verify signature + update DB, rồi redirect sang trang kết quả sạch sẽ.
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

        boolean isSuccess = isValid && "00".equals(responseCode)
                && ("00".equals(transStatus) || transStatus == null);

        if (isSuccess && orderCode != null) {
            long amount = amountStr != null ? Long.parseLong(amountStr) : 0;
            vnpay.processSuccess(orderCode, transactionNo, amount, bankCode);
        } else if (!isSuccess && orderCode != null) {
            vnpay.processFailed(orderCode, responseCode);
        }

        // Redirect tới trang kết quả — chỉ truyền orderCode, không lộ tham số VNPay
        String status = isSuccess ? "success" : "failed";
        String msg = getResponseMessage(responseCode);
        String encodedMsg = URLEncoder.encode(msg, StandardCharsets.UTF_8);

        resp.sendRedirect(req.getContextPath() + "/payment/result?status=" + status
                + "&orderCode=" + orderCode + "&message=" + encodedMsg
                + "&transactionNo=" + (transactionNo != null ? transactionNo : "")
                + "&bankCode=" + (bankCode != null ? bankCode : ""));
    }

    /** Diễn giải mã lỗi VNPay thành thông báo. */
    private String getResponseMessage(String code) {
        if (code == null) return "Lỗi không xác định";
        return switch (code) {
            case "00" -> "Giao dịch thành công!";
            case "07" -> "Giao dịch bị nghi ngờ gian lận.";
            case "09" -> "Thẻ chưa đăng ký dịch vụ InternetBanking.";
            case "10" -> "Xác thực thông tin thẻ không đúng quá 3 lần.";
            case "11" -> "Đã hết hạn chờ thanh toán.";
            case "12" -> "Thẻ bị khóa.";
            case "13" -> "Sai mật khẩu OTP.";
            case "24" -> "Khách hàng hủy giao dịch.";
            case "51" -> "Tài khoản không đủ số dư.";
            case "65" -> "Vượt quá hạn mức giao dịch trong ngày.";
            case "75" -> "Ngân hàng đang bảo trì.";
            case "79" -> "Sai mật khẩu thanh toán quá số lần quy định.";
            default -> "Giao dịch thất bại. Mã lỗi: " + code;
        };
    }
}
