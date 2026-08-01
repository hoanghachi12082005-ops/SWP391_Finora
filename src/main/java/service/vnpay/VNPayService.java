package service.vnpay;

import dao.inventory.InventoryDAO;
import dao.sales.CustomerPointDAO;
import dao.sales.OrderDAO;
import dao.sales.OrderDetailDAO;
import jakarta.servlet.http.HttpServletRequest;
import model.Order;
import model.OrderDetail;
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
    private final OrderDetailDAO orderDetailDAO = new OrderDetailDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
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

        String hashData = buildHashData(params);
        String secureHash = Config.hmacSHA512(hashData);
        return Config.PAY_URL + "?" + hashData + "&vnp_SecureHash=" + urlEncode(secureHash);
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

    /** Xử lý thanh toán thành công: cập nhật trạng thái order + tích điểm. */
    public boolean processSuccess(String orderCode, String transactionNo, long amount, String bankCode) {
        try (Connection conn = DBContext.getConnection()) {
            int orderId = orderDAO.findIdByCode(conn, orderCode);
            if (orderId == 0) return false;

            String status = orderDAO.getStatus(conn, orderId);
            if ("COMPLETED".equals(status) || "PAID".equals(status)) return false;

            orderDAO.updateStatus(conn, orderId, "COMPLETED");

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

    /** Xử lý thanh toán thất bại: cập nhật trạng thái order + hoàn kho + hoàn voucher. */
    public boolean processFailed(String orderCode, String responseCode) {
        try (Connection conn = DBContext.getConnection()) {
            int orderId = orderDAO.findIdByCode(conn, orderCode);
            if (orderId == 0) return false;

            String status = orderDAO.getStatus(conn, orderId);
            if ("COMPLETED".equals(status) || "PAID".equals(status)) return false;

            String newStatus = "24".equals(responseCode) ? "CANCELLED" : "FAILED";
            orderDAO.updateStatus(conn, orderId, newStatus);

            // Hoàn kho: trả lại số lượng tồn kho đã trừ
            Order order = orderDAO.findByCode(conn, orderCode);
            if (order != null) {
                int warehouseId = order.getWarehouseId();
                List<OrderDetail> details = orderDetailDAO.findByOrderId(conn, orderId);
                for (OrderDetail d : details) {
                    inventoryDAO.increaseStock(conn, warehouseId, d.getProductId(), d.getQuantity());
                }

            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== HELPERS ====================

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
