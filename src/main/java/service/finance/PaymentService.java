package service.finance;

import dao.finance.ExpenseVoucherDAO;
import dao.finance.PaymentDAO;
import dao.finance.ReceiptVoucherDAO;
import dao.report.SalesTransactionReportDAO;
import dao.sales.OrderDAO;
import model.ExpenseVoucher;
import model.Order;
import model.Payment;
import model.ReceiptVoucher;
import model.SalesTransaction;
import model.SalesTransactionFilter;
import model.SalesTransactionKpi;
import util.database.DBContext;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class PaymentService {
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final ReceiptVoucherDAO receiptVoucherDAO = new ReceiptVoucherDAO();
    private final ExpenseVoucherDAO expenseVoucherDAO = new ExpenseVoucherDAO();
    private final SalesTransactionReportDAO reportDAO = new SalesTransactionReportDAO();

    public List<Payment> getTransactionsPaging(
            String keyword,
            String type,
            String paymentMethod,
            String timeRange,
            int page,
            int pageSize) {
        return paymentDAO.getTransactionsPaging(keyword, type, paymentMethod, null, null, timeRange, null, page, pageSize);
    }

    public List<Payment> getTransactionsPaging(
            String keyword,
            String type,
            String paymentMethod,
            String timeRange,
            Integer branchId,
            int page,
            int pageSize) {
        return paymentDAO.getTransactionsPaging(keyword, type, paymentMethod, null, null, timeRange, branchId, page, pageSize);
    }

    public List<Payment> getTransactionsPaging(
            String keyword,
            String type,
            String paymentMethod,
            String fromDate,
            String toDate,
            String timeRange,
            Integer branchId,
            int page,
            int pageSize) {
        return paymentDAO.getTransactionsPaging(keyword, type, paymentMethod, fromDate, toDate, timeRange, branchId, page, pageSize);
    }

    public int countTransactions(
            String keyword,
            String type,
            String paymentMethod,
            String timeRange) {
        return paymentDAO.countTransactions(keyword, type, paymentMethod, null, null, timeRange, null);
    }

    public int countTransactions(
            String keyword,
            String type,
            String paymentMethod,
            String timeRange,
            Integer branchId) {
        return paymentDAO.countTransactions(keyword, type, paymentMethod, null, null, timeRange, branchId);
    }

    public int countTransactions(
            String keyword,
            String type,
            String paymentMethod,
            String fromDate,
            String toDate,
            String timeRange,
            Integer branchId) {
        return paymentDAO.countTransactions(keyword, type, paymentMethod, fromDate, toDate, timeRange, branchId);
    }

    public double getTotalCashBalance() {
        return paymentDAO.getTotalCashBalance(null);
    }

    public double getTotalCashBalance(Integer branchId) {
        return paymentDAO.getTotalCashBalance(branchId);
    }

    public double getTotalBankBalance() {
        return paymentDAO.getTotalBankBalance(null);
    }

    public double getTotalBankBalance(Integer branchId) {
        return paymentDAO.getTotalBankBalance(branchId);
    }

    public double getSumIncome(String paymentMethod) {
        return paymentDAO.getSumIncome(paymentMethod, null);
    }

    public double getSumIncome(String paymentMethod, Integer branchId) {
        return paymentDAO.getSumIncome(paymentMethod, branchId);
    }

    public double getSumExpense(String paymentMethod) {
        return paymentDAO.getSumExpense(paymentMethod, null);
    }

    public double getSumExpense(String paymentMethod, Integer branchId) {
        return paymentDAO.getSumExpense(paymentMethod, branchId);
    }

    public List<Map<String, Object>> getWeeklyOverview(String keyword, String type, String paymentMethod, String timeRange) {
        return paymentDAO.getWeeklyOverview(keyword, type, paymentMethod, null, null, timeRange, null);
    }

    public List<Map<String, Object>> getWeeklyOverview(String keyword, String type, String paymentMethod, String timeRange, Integer branchId) {
        return paymentDAO.getWeeklyOverview(keyword, type, paymentMethod, null, null, timeRange, branchId);
    }

    public List<Map<String, Object>> getWeeklyOverview(String keyword, String type, String paymentMethod, String fromDate, String toDate, String timeRange, Integer branchId) {
        return paymentDAO.getWeeklyOverview(keyword, type, paymentMethod, fromDate, toDate, timeRange, branchId);
    }

    public boolean insert(Payment payment) {
        return paymentDAO.insert(payment);
    }

    public String createReceipt(int orderId, Payment payment, int employeeId, int branchId) throws Exception {
        return createVoucher(orderId, payment, employeeId, branchId, true);
    }

    public String createExpense(int orderId, Payment payment, int employeeId, int branchId) throws Exception {
        return createVoucher(orderId, payment, employeeId, branchId, false);
    }

    public List<Order> getRecentOrders(int limit) {
        return orderDAO.getRecentSaleOrders(limit);
    }

    public SalesTransactionKpi getTransactionKpi(SalesTransactionFilter f) {
        return reportDAO.calculateKpi(f);
    }

    public List<SalesTransaction> searchTransactions(SalesTransactionFilter f, int page, int pageSize) {
        return reportDAO.searchTransactions(f, page, pageSize);
    }

    public int countTransactions(SalesTransactionFilter f) {
        return reportDAO.countTransactions(f);
    }

    public List<String> getDistinctTransactionTypes() {
        return reportDAO.getDistinctTransactionTypes();
    }

    private String createVoucher(int orderId, Payment payment, int employeeId, int branchId, boolean isReceipt) throws Exception {
        String prefix = isReceipt ? "PT" : "PC";
        String voucherPrefix = isReceipt ? "PTV" : "PCV";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Order existing = orderDAO.findById(orderId);
                if (existing == null) {
                    throw new Exception("Đơn hàng không tồn tại: " + orderId);
                }

                payment.setOrderId(orderId);
                payment.setEmployeeId(employeeId);
                payment.setBranchId(branchId);
                payment.setStatus("PAID");

                String code = paymentDAO.generateTransactionCode(payment.getPaymentType(), prefix);
                payment.setName(code);

                int paymentId = paymentDAO.insert(conn, payment);

                // Voucher amount must match Payment amount
                if (payment.getAmount() != payment.getAmount()) {
                    throw new Exception("Số tiền phiếu không khớp với số tiền thanh toán.");
                }
                // Payment status must be valid
                if (payment.getStatus() == null || (!"PAID".equals(payment.getStatus()) && !"PENDING".equals(payment.getStatus()))) {
                    throw new Exception("Trạng thái thanh toán không hợp lệ.");
                }

                String voucherNumber = paymentDAO.generateTransactionCode(payment.getPaymentType(), voucherPrefix);

                if (isReceipt) {
                    ReceiptVoucher v = new ReceiptVoucher();
                    v.setPaymentId(paymentId);
                    v.setVoucherNumber(voucherNumber);
                    v.setAmount(payment.getAmount());
                    v.setCreatedBy(employeeId);
                    receiptVoucherDAO.insert(conn, v);
                } else {
                    ExpenseVoucher v = new ExpenseVoucher();
                    v.setPaymentId(paymentId);
                    v.setVoucherNumber(voucherNumber);
                    v.setAmount(payment.getAmount());
                    v.setCreatedBy(employeeId);
                    expenseVoucherDAO.insert(conn, v);
                }

                conn.commit();
                return null;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
