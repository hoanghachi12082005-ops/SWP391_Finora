package service.vnpay;

import dao.finance.PaymentDAO;
import dao.sales.CustomerPointDAO;
import dao.sales.OrderDAO;
import jakarta.servlet.http.HttpServletRequest;
import model.Order;
import model.Payment;
import util.database.DBContext;
import util.vnpay.Config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Service xử lý toàn bộ logic thanh toán VNPay.
 * Gom hết logic rải rác từ các Servlet vào đây.
 */
public class VNPayService {

    private final OrderDAO orderDAO = new OrderDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    // ==================== TẠO LINK THANH TOÁN ====================

    /** Tạo URL thanh toán VNPay để redirect trình duyệt. */
    public String buildPaymentUrl(String orderCode, long amount, String ipAddr, String returnUrl) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Version", Config.VERSION);
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", Config.TMN_CODE);
        params.put("vnp_Amount", String.valueOf(amount * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", orderCode);
        params.put("vnp_OrderInfo", "Thanh toan don hang " + orderCode);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", ipAddr);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
        params.put("vnp_CreateDate", fmt.format(cal.getTime()));
        cal.add(Calendar.MINUTE, 15);
        params.put("vnp_ExpireDate", fmt.format(cal.getTime()));

        return Config.PAY_URL + "?" + buildSignedQuery(params);
    }

    // ==================== XỬ LÝ CALLBACK ====================

    /** Đọc toàn bộ params từ request (dùng cho cả Return lẫn IPN). */
    public Map<String, String> extractParams(HttpServletRequest req) {
        Map<String, String> params = new LinkedHashMap<>();
        Enumeration<String> names = req.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String value = req.getParameter(name);
            if (value != null && !value.isEmpty()) {
                params.put(name, value);
            }
        }
        return params;
    }

    /** Kiểm tra chữ ký VNPay có hợp lệ không. */
    public boolean verifySignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null) return false;

        // Loại bỏ trường hash trước khi rebuild
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        String calculated = Config.hmacSHA512(buildHashData(params));
        return calculated.equalsIgnoreCase(receivedHash);
    }

    /** Xử lý thanh toán thành công: cập nhật order + tạo Payment. */
    public boolean processSuccess(String orderCode, String transactionNo, long amount, String bankCode) {
        try (Connection conn = DBContext.getConnection()) {
            int orderId = orderDAO.findIdByCode(conn, orderCode);
            if (orderId == 0) return false;

            String status = orderDAO.getStatus(conn, orderId);
            if ("COMPLETED".equals(status) || "PAID".equals(status)) return false;

            orderDAO.updateStatus(conn, orderId, "COMPLETED");

            Payment payment = new Payment();
            payment.setOrderId(orderId);
            payment.setAmount(amount / 100.0);
            payment.setStatus("PAID");
            payment.setName("VNPAY-" + (transactionNo != null ? transactionNo : "N/A"));
            payment.setMethod("VNPAY");
            payment.setPaymentType("INCOME");
            payment.setDescription("Thanh toán VNPAY đơn hàng " + orderCode
                    + ", GD: " + (transactionNo != null ? transactionNo : "N/A"));

            paymentDAO.insert(conn, payment);

            // Earn loyalty points
            Order order = orderDAO.findByCode(conn, orderCode);
            if (order != null && order.getCustomerId() != null && order.getCustomerId() > 0) {
                new CustomerPointDAO().addPoints(conn, order.getCustomerId(), order.getTotalAmount(), orderId);
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Xử lý thanh toán thất bại: cập nhật trạng thái order. */
    public boolean processFailed(String orderCode, String responseCode) {
        try (Connection conn = DBContext.getConnection()) {
            int orderId = orderDAO.findIdByCode(conn, orderCode);
            if (orderId == 0) return false;

            String status = orderDAO.getStatus(conn, orderId);
            if ("COMPLETED".equals(status) || "PAID".equals(status)) return false;

            String newStatus = "24".equals(responseCode) ? "CANCELLED" : "FAILED";
            orderDAO.updateStatus(conn, orderId, newStatus);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== TOKEN KÉP CHO RESULT PAGE ====================
    // Token chứa TOÀN BỘ dữ liệu kết quả, mã hóa dạng:
    //   base64UrlSafe(payload) + "." + base64UrlSafe(hmacSHA512(payload))
    // trong đó payload = orderCode|status|message|amount|transactionNo|bankCode|payDate

    /** Tạo token chứa toàn bộ kết quả, URL chỉ hiện ?t=... */
    public String encodeResultToken(String orderCode, String status, String message,
                                     String amount, String transactionNo,
                                     String bankCode, String payDate) {
        String payload = String.join("|",
                nvl(orderCode), nvl(status), nvl(message), nvl(amount),
                nvl(transactionNo), nvl(bankCode), nvl(payDate));
        String hmac = Config.hmacSHA512(payload);
        String b64Payload = Base64.getUrlEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String b64Hmac    = Base64.getUrlEncoder().encodeToString(hmac.getBytes(StandardCharsets.UTF_8));
        return b64Payload + "." + b64Hmac;
    }

    /** Giải mã token, trả về Map chứa các field. Trả về null nếu token không hợp lệ. */
    public Map<String, String> decodeResultToken(String token) {
        if (token == null) return null;
        String[] parts = token.split("\\.", 2);
        if (parts.length != 2) return null;

        try {
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String hmac    = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            // Verify HMAC
            String expected = Config.hmacSHA512(payload);
            if (!hmac.equalsIgnoreCase(expected)) return null;

            String[] fields = payload.split("\\|", 7);
            Map<String, String> map = new LinkedHashMap<>();
            String[] keys = {"orderCode", "status", "message", "amount", "transactionNo", "bankCode", "payDate"};
            for (int i = 0; i < keys.length; i++) {
                map.put(keys[i], i < fields.length ? fields[i] : "");
            }
            return map;
        } catch (Exception e) {
            return null;
        }
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }

    /** Diễn giải mã lỗi VNPay thành thông báo. */
    public String getResponseMessage(String code) {
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

    // ==================== XỬ LÝ TRANSACTION (cho IPN) ====================

    /** Xử lý thành công trong transaction (dùng cho IPN). */
    public void processSuccessInTransaction(Connection conn, String orderCode,
                                             String transactionNo, long amount, String bankCode) throws Exception {
        int orderId = orderDAO.findIdByCode(conn, orderCode);
        if (orderId == 0) throw new Exception("Order not found: " + orderCode);

        String status = orderDAO.getStatus(conn, orderId);
        if ("COMPLETED".equals(status) || "PAID".equals(status)) {
            throw new Exception("Order already confirmed: " + orderCode);
        }

        orderDAO.updateStatus(conn, orderId, "COMPLETED");

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount / 100.0);
        payment.setStatus("PAID");
        payment.setName("VNPAY-" + (transactionNo != null ? transactionNo : "N/A"));
        payment.setMethod("VNPAY");
        payment.setPaymentType("INCOME");
        payment.setDescription("Thanh toán VNPAY đơn hàng " + orderCode
                + ", GD: " + (transactionNo != null ? transactionNo : "N/A"));

        paymentDAO.insert(conn, payment);

        // Earn loyalty points
        Order order = orderDAO.findByCode(conn, orderCode);
        if (order != null && order.getCustomerId() != null && order.getCustomerId() > 0) {
            new CustomerPointDAO().addPoints(conn, order.getCustomerId(), order.getTotalAmount(), orderId);
        }
    }

    /** Xử lý thất bại trong transaction (dùng cho IPN). */
    public void processFailedInTransaction(Connection conn, String orderCode, String responseCode) throws Exception {
        int orderId = orderDAO.findIdByCode(conn, orderCode);
        if (orderId == 0) throw new Exception("Order not found: " + orderCode);

        String newStatus = "24".equals(responseCode) ? "CANCELLED" : "FAILED";
        orderDAO.updateStatus(conn, orderId, newStatus);
    }

    // ==================== HELPERS ====================

    /** Sắp xếp params + tạo chữ ký + trả về query string. */
    private String buildSignedQuery(Map<String, String> params) {
        String hashData = buildHashData(params);
        String secureHash = Config.hmacSHA512(hashData);
        return hashData + "&vnp_SecureHash=" + urlEncode(secureHash);
    }

    /** Sắp xếp params theo thứ tự alphabet và nối thành chuỗi k=v&k=v. */
    private String buildHashData(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String key = it.next();
            String value = params.get(key);
            if (value != null && !value.isEmpty()) {
                sb.append(key).append('=').append(urlEncode(value));
                if (it.hasNext()) sb.append('&');
            }
        }
        return sb.toString();
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
