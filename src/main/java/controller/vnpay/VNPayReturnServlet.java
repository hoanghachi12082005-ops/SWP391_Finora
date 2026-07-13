package controller.vnpay;

import dao.sales.OrderDAO;
import dao.finance.PaymentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Payment;
import util.database.DBContext;
import util.vnpay.Config;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Xử lý kết quả thanh toán VNPay trả về qua browser (Return URL).
 * Khi VNPay redirect trình duyệt về URL này, ta cập nhật trạng thái đơn hàng
 * (vì VNPay server không thể gọi IPN tới localhost).
 * GET /vnpay/return
 */
@WebServlet("/vnpay/return")
public class VNPayReturnServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Lấy tất cả tham số từ VNPay
        Map<String, String> vnp_Params = new LinkedHashMap<>();
        Enumeration<String> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = req.getParameter(paramName);
            if (paramValue != null && !paramValue.isEmpty()) {
                vnp_Params.put(paramName, paramValue);
            }
        }

        String vnp_SecureHash = vnp_Params.get("vnp_SecureHash");
        if (vnp_SecureHash == null) {
            req.setAttribute("message", "Thiếu chữ ký bảo mật.");
            req.setAttribute("status", "error");
            req.getRequestDispatcher("/views/common/vnpay_result.jsp").forward(req, resp);
            return;
        }

        vnp_Params.remove("vnp_SecureHash");
        vnp_Params.remove("vnp_SecureHashType");

        // Sắp xếp tham số và kiểm tra checksum
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                if (itr.hasNext()) hashData.append('&');
            }
        }

        String calculatedHash = Config.hmacSHA512(Config.vnp_HashSecret, hashData.toString());
        boolean isValidSig = calculatedHash.equalsIgnoreCase(vnp_SecureHash);

        String vnp_ResponseCode = vnp_Params.get("vnp_ResponseCode");
        String vnp_TransactionStatus = vnp_Params.get("vnp_TransactionStatus");
        String vnp_TxnRef = vnp_Params.get("vnp_TxnRef");
        String vnp_Amount = vnp_Params.get("vnp_Amount");
        String vnp_TransactionNo = vnp_Params.get("vnp_TransactionNo");
        String vnp_BankCode = vnp_Params.get("vnp_BankCode");
        String vnp_PayDate = vnp_Params.get("vnp_PayDate");

        boolean isSuccess = isValidSig && "00".equals(vnp_ResponseCode)
                && ("00".equals(vnp_TransactionStatus) || vnp_TransactionStatus == null);

        // ── Cập nhật trạng thái đơn hàng ──
        if (isSuccess && vnp_TxnRef != null) {
            updateOrderSuccess(vnp_TxnRef, vnp_TransactionNo, vnp_Amount, vnp_BankCode);
        } else if (!isSuccess && vnp_TxnRef != null) {
            updateOrderFailed(vnp_TxnRef, vnp_ResponseCode);
        }

        req.setAttribute("status", isSuccess ? "success" : "error");
        req.setAttribute("message", getResponseMessage(vnp_ResponseCode));
        req.setAttribute("orderCode", vnp_TxnRef);
        req.setAttribute("amount", vnp_Amount != null ? Long.parseLong(vnp_Amount) / 100 : 0);
        req.setAttribute("transactionNo", vnp_TransactionNo);
        req.setAttribute("bankCode", vnp_BankCode);
        req.setAttribute("payDate", vnp_PayDate);

        req.getRequestDispatcher("/views/common/vnpay_result.jsp").forward(req, resp);
    }

    /**
     * Cập nhật đơn hàng thành COMPLETED + tạo Payment record.
     * Dùng làm fallback khi VNPay không gọi được IPN (localhost).
     * Tách thành 2 transaction riêng để nếu insert Payment lỗi thì
     * trạng thái order vẫn được cập nhật.
     */
    private void updateOrderSuccess(String orderCode, String transactionNo, String amountStr, String bankCode) {
        Connection conn = null;
        try {
            conn = DBContext.getConnection();

            // ─── Bước 1: Cập nhật trạng thái order ───
            OrderDAO orderDAO = new OrderDAO();
            int orderId = orderDAO.findIdByCode(conn, orderCode);
            if (orderId == 0) {
                System.err.println("[VNPayReturn] Không tìm thấy order: " + orderCode);
                return;
            }

            String currentStatus = orderDAO.getStatus(conn, orderId);
            if ("COMPLETED".equals(currentStatus) || "PAID".equals(currentStatus)) {
                System.out.println("[VNPayReturn] Order " + orderCode + " đã hoàn tất, bỏ qua.");
                return;
            }

            orderDAO.updateStatus(conn, orderId, "COMPLETED");
            System.out.println("[VNPayReturn] Đã cập nhật order " + orderCode + " -> COMPLETED");

            // ─── Bước 2: Tạo Payment record (transaction riêng) ───
            try {
                Payment payment = new Payment();
                payment.setOrderId(orderId);
                payment.setPaymentAmount(amountStr != null ? Long.parseLong(amountStr) / 100.0 : 0);
                payment.setPaymentStatus("PAID");
                payment.setTransactionCode("VNPAY-" + (transactionNo != null ? transactionNo : "N/A"));
                payment.setPaymentType("INCOME");
                payment.setMethod("VNPAY");
                payment.setDescription("Thanh toán VNPAY đơn hàng " + orderCode + ", GD: " + (transactionNo != null ? transactionNo : "N/A"));

                PaymentDAO paymentDAO = new PaymentDAO();
                paymentDAO.insert(conn, payment);
                System.out.println("[VNPayReturn] Đã tạo Payment record cho order " + orderCode);
            } catch (Exception e) {
                // Lỗi insert Payment không làm ảnh hưởng tới trạng thái order
                System.err.println("[VNPayReturn] Lỗi tạo Payment record (order " + orderCode + "): " + e.getMessage());
                e.printStackTrace();
            }

        } catch (SQLException e) {
            System.err.println("[VNPayReturn] Lỗi SQL khi cập nhật order " + orderCode + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[VNPayReturn] Lỗi khi cập nhật order " + orderCode + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    /**
     * Cập nhật đơn hàng thất bại khi VNPay trả về lỗi hoặc người dùng hủy.
     * - Mã 24 (khách hủy) → CANCELLED
     * - Các mã lỗi khác → FAILED
     */
    private void updateOrderFailed(String orderCode, String responseCode) {
        try (Connection conn = DBContext.getConnection()) {
            OrderDAO orderDAO = new OrderDAO();
            int orderId = orderDAO.findIdByCode(conn, orderCode);
            if (orderId == 0) {
                System.err.println("[VNPayReturn] Không tìm thấy order: " + orderCode);
                return;
            }

            String currentStatus = orderDAO.getStatus(conn, orderId);
            if ("COMPLETED".equals(currentStatus) || "PAID".equals(currentStatus)) {
                System.out.println("[VNPayReturn] Order " + orderCode + " đã hoàn tất, bỏ qua.");
                return;
            }

            String newStatus = "24".equals(responseCode) ? "CANCELLED" : "FAILED";
            orderDAO.updateStatus(conn, orderId, newStatus);
            System.out.println("[VNPayReturn] Đã cập nhật order " + orderCode + " -> " + newStatus
                    + " (mã VNPay: " + responseCode + ")");

        } catch (SQLException e) {
            System.err.println("[VNPayReturn] Lỗi SQL khi cập nhật order " + orderCode + ": " + e.getMessage());
        }
    }

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
