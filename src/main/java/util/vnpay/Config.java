package util.vnpay;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Cấu hình và tiện ích cho VNPay.
 * Chỉ giữ lại những gì thực sự dùng đến.
 */
public final class Config {

    public static final String TMN_CODE   = "FHVORUUC";
    public static final String HASH_SECRET = "TCE83JZ6EZP79YVT7IMN8I57472WFQ07";
    public static final String PAY_URL    = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String VERSION    = "2.1.0";

    private Config() {}

    /** Tạo chữ ký HMAC-SHA512 từ dữ liệu đầu vào. */
    public static String hmacSHA512(String data) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(HASH_SECRET.getBytes("UTF-8"), "HmacSHA512");
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(keySpec);
            byte[] bytes = mac.doFinal(data.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi tạo HMAC-SHA512", ex);
        }
    }

    /** Lấy địa chỉ IP thực của client (qua proxy nếu có). */
    public static String getIpAddress(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = req.getHeader("Proxy-Client-IP");
        if (ip == null || ip.isEmpty()) ip = req.getHeader("WL-Proxy-Client-IP");
        if (ip == null || ip.isEmpty()) ip = req.getRemoteAddr();
        return ip;
    }
}
