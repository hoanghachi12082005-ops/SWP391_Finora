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
import dao.system.ActivityLogDAO;
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
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    public List<Payment> getTransactionsPaging(
            String keyword,
            String type,
            String paymentMethod,
            String timeRange,
            int page,
            int pageSize) {
        return paymentDAO.getTransactionsPaging(keyword, type, null, paymentMethod, null, null, null, null, null, timeRange, null, page, pageSize);
    }

    public List<Payment> getTransactionsPaging(
            String keyword,
            String type,
            String paymentMethod,
            String timeRange,
            Integer branchId,
            int page,
            int pageSize) {
        return paymentDAO.getTransactionsPaging(keyword, type, null, paymentMethod, null, null, null, null, null, timeRange, branchId, page, pageSize);
    }

    public List<Payment> getTransactionsPaging(
            String keyword,
            String type,
            String fromDate,
            String toDate,
            String timeRange,
            Integer branchId,
            int page,
            int pageSize) {
        return paymentDAO.getTransactionsPaging(keyword, type, null, null, null, null, null, fromDate, toDate, timeRange, branchId, page, pageSize);
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
        return paymentDAO.getTransactionsPaging(keyword, type, null, paymentMethod, null, null, null, fromDate, toDate, timeRange, branchId, page, pageSize);
    }

    public List<Payment> getTransactionsPaging(
            String keyword,
            String type,
            String orderType,
            String paymentMethod,
            Integer employeeId,
            Double amountFrom,
            Double amountTo,
            String fromDate,
            String toDate,
            String timeRange,
            Integer branchId,
            int page,
            int pageSize) {
        return paymentDAO.getTransactionsPaging(keyword, type, orderType, paymentMethod, employeeId, amountFrom, amountTo, fromDate, toDate, timeRange, branchId, page, pageSize);
    }

    public int countTransactions(
            String keyword,
            String type,
            String paymentMethod,
            String timeRange) {
        return paymentDAO.countTransactions(keyword, type, null, paymentMethod, null, null, null, null, null, timeRange, null);
    }

    public int countTransactions(
            String keyword,
            String type,
            String paymentMethod,
            String timeRange,
            Integer branchId) {
        return paymentDAO.countTransactions(keyword, type, null, paymentMethod, null, null, null, null, null, timeRange, branchId);
    }

    public int countTransactions(
            String keyword,
            String type,
            String paymentMethod,
            String fromDate,
            String toDate,
            String timeRange,
            Integer branchId) {
        return paymentDAO.countTransactions(keyword, type, null, paymentMethod, null, null, null, fromDate, toDate, timeRange, branchId);
    }

    public int countTransactions(
            String keyword,
            String type,
            String orderType,
            String paymentMethod,
            Integer employeeId,
            Double amountFrom,
            Double amountTo,
            String fromDate,
            String toDate,
            String timeRange,
            Integer branchId) {
        return paymentDAO.countTransactions(keyword, type, orderType, paymentMethod, employeeId, amountFrom, amountTo, fromDate, toDate, timeRange, branchId);
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

    public String createReceipt(Payment payment, int employeeId, int branchId) throws Exception {
        return createVoucher(payment, employeeId, branchId, true);
    }

    public String createExpense(Payment payment, int employeeId, int branchId) throws Exception {
        return createVoucher(payment, employeeId, branchId, false);
    }

    public String createReceipt(int orderId, Payment payment, int employeeId, int branchId) throws Exception {
        payment.setOrderId(orderId);
        return createReceipt(payment, employeeId, branchId);
    }

    public String createExpense(int orderId, Payment payment, int employeeId, int branchId) throws Exception {
        payment.setOrderId(orderId);
        return createExpense(payment, employeeId, branchId);
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

    private String createVoucher(Payment payment, int employeeId, int branchId, boolean isReceipt) throws Exception {
        String paymentType = isReceipt ? "INCOME" : "EXPENSE";
        String prefix = isReceipt ? "PT" : "PC";
        String orderCodePrefix = isReceipt ? "ORD-RV-" : "ORD-PV-";
        String voucherPrefix = isReceipt ? "PTV" : "PCV";

        if (payment.getAmount() <= 0) {
            throw new Exception("Số tiền phiếu phải lớn hơn 0.");
        }

        if (!isReceipt) {
            double balance = 0;
            if ("CASH".equals(payment.getMethod())) {
                balance = getTotalCashBalance(branchId);
            } else if ("BANK_TRANSFER".equals(payment.getMethod())) {
                balance = getTotalBankBalance(branchId);
            }
            if (payment.getAmount() > balance) {
                throw new Exception("Số dư quỹ tiền mặt không đủ để thực hiện chi khoản này.");
            }
        }

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Create specialized Order (order_type = OTHER, status = COMPLETED)
                Order order = new Order();
                String orderCode = orderCodePrefix + String.format("%06d", System.currentTimeMillis() % 1000000);
                order.setOrderCode(orderCode);
                order.setOrderType("OTHER");
                order.setStatus(Order.OrderStatus.COMPLETED);
                order.setBranchId(branchId);
                order.setEmpId(employeeId);
                order.setWarehouseId(1);
                order.setSubtotal(payment.getAmount());
                order.setDiscountAmount(0.0);
                order.setTotalAmount(payment.getAmount());
                order.setPaymentMethod(payment.getMethod() != null ? payment.getMethod() : "CASH");

                int orderId = orderDAO.createOrderInTransaction(conn, order);

                // 2. Create Payment linked to Order (payment_status = PAID)
                payment.setOrderId(orderId);
                payment.setEmployeeId(employeeId);
                payment.setBranchId(branchId);
                payment.setPaymentType(paymentType);
                payment.setStatus("PAID");

                String code = paymentDAO.generateTransactionCode(conn, paymentType, prefix);
                payment.setName(code);

                int paymentId = paymentDAO.insert(conn, payment);

                conn.commit();

                // Audit log
                try {
                    String actionName = isReceipt ? "TẠO_PHIẾU_THU" : "TẠO_PHIẾU_CHI";
                    String logMsg = (isReceipt ? "Tạo phiếu thu " : "Tạo phiếu chi ") + code + " cho đơn hàng " + orderCode + " - Số tiền: " + payment.getAmount();
                    activityLogDAO.insertLog(employeeId, actionName, "cashbook", paymentId, null, logMsg);
                } catch (Exception ignored) {}

                return null;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
