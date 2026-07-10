package controller.vnpay;

import dao.sales.OrderDAO;
import dao.finance.PaymentDAO;
import model.Payment;
import util.database.DBContext;
import util.vnpay.Config;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * IPN (Instant Payment Notification) - Server-to-server callback từ VNPAY.
 * Cập nhật trạng thái đơn hàng sau khi VNPAY xác nhận thanh toán.
 * GET /vnpay/ipn
 */
@WebServlet("/vnpay/ipn")
public class VNPayIPNServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        // Lấy và verify checksum
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
        vnp_Params.remove("vnp_SecureHash");
        vnp_Params.remove("vnp_SecureHashType");

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

        if (!calculatedHash.equalsIgnoreCase(vnp_SecureHash)) {
            out.print(jsonResponse("97", "Invalid signature"));
            return;
        }

        String orderCode = vnp_Params.get("vnp_TxnRef");
        String vnp_ResponseCode = vnp_Params.get("vnp_ResponseCode");
        String vnp_TransactionStatus = vnp_Params.get("vnp_TransactionStatus");
        String vnp_Amount = vnp_Params.get("vnp_Amount");
        String vnp_TransactionNo = vnp_Params.get("vnp_TransactionNo");
        String vnp_BankCode = vnp_Params.get("vnp_BankCode");

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            OrderDAO orderDAO = new OrderDAO();
            int orderId = orderDAO.findIdByCode(conn, orderCode);
            if (orderId == 0) {
                conn.rollback();
                out.print(jsonResponse("01", "Order not found"));
                return;
            }

            String currentStatus = orderDAO.getStatus(conn, orderId);
            if ("COMPLETED".equals(currentStatus) || "PAID".equals(currentStatus)) {
                conn.rollback();
                out.print(jsonResponse("02", "Order already confirmed"));
                return;
            }

            boolean isSuccess = "00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus);

            if (isSuccess) {
                orderDAO.updateStatus(conn, orderId, "COMPLETED");

                Payment payment = new Payment();
                payment.setOrderId(orderId);
                payment.setPaymentAmount(Long.parseLong(vnp_Amount) / 100.0);
                payment.setPaymentStatus("PAID");
                payment.setTransactionCode("VNPAY-" + vnp_TransactionNo);
                payment.setPaymentType("INCOME");
                payment.setMethod("VNPAY");
                payment.setDescription("Thanh toán VNPAY đơn hàng " + orderCode + ", GD: " + vnp_TransactionNo);

                PaymentDAO paymentDAO = new PaymentDAO();
                paymentDAO.insert(conn, payment);

                conn.commit();
                out.print(jsonResponse("00", "Confirm Success"));
            } else {
                orderDAO.updateStatus(conn, orderId, "FAILED");
                conn.commit();
                out.print(jsonResponse("00", "Confirm Success"));
            }

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            out.print(jsonResponse("99", "Database error: " + e.getMessage()));
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            out.print(jsonResponse("99", "Unknow error"));
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }

    private String jsonResponse(String rspCode, String message) {
        return "{\"RspCode\":\"" + rspCode + "\",\"Message\":\"" + message + "\"}";
    }
}
