package service.finance;

import dao.finance.PaymentDAO;
import model.Payment;

import java.util.List;
import java.util.Map;

public class PaymentService {
    private final PaymentDAO dao = new PaymentDAO();

    public List<Payment> getTransactionsPaging(
            String keyword,
            String type,
            String paymentMethod,
            String timeRange,
            int page,
            int pageSize) {
        return dao.getTransactionsPaging(keyword, type, paymentMethod, timeRange, page, pageSize);
    }

    public int countTransactions(
            String keyword,
            String type,
            String paymentMethod,
            String timeRange) {
        return dao.countTransactions(keyword, type, paymentMethod, timeRange);
    }

    public double getTotalCashBalance() {
        return dao.getTotalCashBalance();
    }

    public double getTotalBankBalance() {
        return dao.getTotalBankBalance();
    }

    public double getSumIncome(String paymentMethod) {
        return dao.getSumIncome(paymentMethod);
    }

    public double getSumExpense(String paymentMethod) {
        return dao.getSumExpense(paymentMethod);
    }

    public List<Map<String, Object>> getWeeklyOverview() {
        return dao.getWeeklyOverview();
    }

    public boolean insert(Payment payment) {
        return dao.insert(payment);
    }
}
