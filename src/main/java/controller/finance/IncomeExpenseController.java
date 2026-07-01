package controller.finance;

import controller.common.BaseController;
import model.Employee;
import model.Payment;
import service.finance.PaymentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "IncomeExpenseController", urlPatterns = {
    "/cashbook", 
    "/cashbook/create-receipt", 
    "/cashbook/create-payment"
})
public class IncomeExpenseController extends BaseController {

    private final PaymentService service = new PaymentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String path = request.getServletPath();
        if ("/cashbook".equals(path)) {
            showCashbook(request, response);
        } else {
            redirect(response, request.getContextPath() + "/cashbook");
        }
    }

    private void showCashbook(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Lấy bộ lọc từ parameters
        String keyword = request.getParameter("keyword");
        String type = request.getParameter("type");
        String paymentMethod = request.getParameter("paymentMethod");
        String timeRange = request.getParameter("timeRange");

        if (timeRange == null) {
            timeRange = "this_month"; // Mặc định là tháng này
        }

        // Phân trang
        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (NumberFormatException ignored) {}
        }
        int pageSize = 10;

        // 2. Lấy dữ liệu danh sách giao dịch
        List<Payment> transactions = service.getTransactionsPaging(keyword, type, paymentMethod, timeRange, page, pageSize);
        int totalRecords = service.countTransactions(keyword, type, paymentMethod, timeRange);
        int totalPage = (int) Math.ceil((double) totalRecords / pageSize);

        // 3. Tính toán các chỉ số quỹ
        double totalCash = service.getTotalCashBalance();
        double totalBank = service.getTotalBankBalance();
        double totalFund = totalCash + totalBank;

        double cashIncome = service.getSumIncome("CASH");
        double cashExpense = service.getSumExpense("CASH");
        double bankIncome = service.getSumIncome("BANK_TRANSFER");
        double bankExpense = service.getSumExpense("BANK_TRANSFER");

        // 4. Lấy dữ liệu biểu đồ tổng quan theo tuần
        List<Map<String, Object>> weeklyStats = service.getWeeklyOverview();
        double[] weeklyIncome = new double[5];
        double[] weeklyExpense = new double[5];
        for (Map<String, Object> stat : weeklyStats) {
            int weekNum = (int) stat.get("weekNum");
            if (weekNum >= 1 && weekNum <= 5) {
                weeklyIncome[weekNum - 1] = (double) stat.get("totalIncome");
                weeklyExpense[weekNum - 1] = (double) stat.get("totalExpense");
            }
        }

        // Chuyển dữ liệu biểu đồ thành chuỗi ngăn cách bởi dấu phẩy
        StringBuilder incomeStr = new StringBuilder();
        StringBuilder expenseStr = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (i > 0) {
                incomeStr.append(",");
                expenseStr.append(",");
            }
            incomeStr.append(weeklyIncome[i]);
            expenseStr.append(weeklyExpense[i]);
        }

        // 5. Đặt các thuộc tính cho JSP
        request.setAttribute("transactions", transactions);
        request.setAttribute("totalRecords", totalRecords);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPage", totalPage);

        request.setAttribute("keyword", keyword);
        request.setAttribute("type", type);
        request.setAttribute("paymentMethod", paymentMethod);
        request.setAttribute("timeRange", timeRange);

        request.setAttribute("totalCash", totalCash);
        request.setAttribute("totalBank", totalBank);
        request.setAttribute("totalFund", totalFund);

        request.setAttribute("cashIncome", cashIncome);
        request.setAttribute("cashExpense", cashExpense);
        request.setAttribute("bankIncome", bankIncome);
        request.setAttribute("bankExpense", bankExpense);

        request.setAttribute("chartIncome", incomeStr.toString());
        request.setAttribute("chartExpense", expenseStr.toString());

        forward(request, response, "payments/list");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();
        Employee user = (Employee) request.getSession().getAttribute("currentUser");

        if ("/cashbook/create-receipt".equals(path) || "/cashbook/create-payment".equals(path)) {
            try {
                double amount = Double.parseDouble(request.getParameter("amount"));
                String method = request.getParameter("method");
                String description = request.getParameter("description");
                String type = "/cashbook/create-receipt".equals(path) ? "INCOME" : "EXPENSE";

                Payment p = new Payment();
                p.setAmount(amount);
                p.setMethod(method);
                p.setDescription(description);
                p.setPaymentType(type);

                if (user != null) {
                    p.setEmployeeId(user.getEmployeeID());
                    p.setBranchId(user.getBranchID());
                }

                boolean success = service.insert(p);
                if (success) {
                    request.getSession().setAttribute("message", type.equals("INCOME") ? "Lập phiếu thu thành công." : "Lập phiếu chi thành công.");
                    request.getSession().setAttribute("messageType", "success");
                } else {
                    request.getSession().setAttribute("message", "Lỗi: Không thể thực hiện giao dịch.");
                    request.getSession().setAttribute("messageType", "danger");
                }
            } catch (Exception e) {
                request.getSession().setAttribute("message", "Lỗi: Dữ liệu nhập vào không hợp lệ.");
                request.getSession().setAttribute("messageType", "danger");
                e.printStackTrace();
            }
        }

        redirect(response, request.getContextPath() + "/cashbook");
    }
}
