package controller.finance;

import controller.common.BaseController;
import model.Employee;
import model.Order;
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

        Employee user = (Employee) request.getSession().getAttribute("currentUser");
        String roleLower = (user != null && user.getRoleName() != null) ? user.getRoleName().trim().toLowerCase() : "";

        if ("admin".equals(roleLower)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin không có quyền truy cập Sổ Quỹ.");
            return;
        }

        Integer targetBranchId = null;
        if ("storemanager".equals(roleLower) || "st manager".equals(roleLower) || roleLower.contains("manager")) {
            // Store Manager strictly accesses their assigned branch ONLY - backend enforced
            targetBranchId = (user != null) ? user.getBranchID() : null;
        } else if ("owner".equals(roleLower)) {
            // Owner can filter by branchId parameter if provided, or view all if empty/null
            String branchParam = request.getParameter("branchId");
            if (branchParam != null && !branchParam.isBlank()) {
                try {
                    targetBranchId = Integer.parseInt(branchParam);
                } catch (NumberFormatException ignored) {}
            }
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập Sổ Quỹ.");
            return;
        }

        String keyword = request.getParameter("keyword");
        String type = request.getParameter("type"); // PaymentType: INCOME, EXPENSE
        String orderType = request.getParameter("orderType"); // OrderType: SALE, PURCHASE, OTHER
        String paymentMethod = request.getParameter("paymentMethod");
        String fromDate = request.getParameter("fromDate");
        String toDate = request.getParameter("toDate");
        String timeRange = request.getParameter("timeRange");
        
        Integer employeeId = null;
        String empIdStr = request.getParameter("employeeId");
        if (empIdStr != null && !empIdStr.isBlank()) {
            try { employeeId = Integer.parseInt(empIdStr); } catch (NumberFormatException ignored) {}
        }

        Double amountFrom = null;
        String amtFromStr = request.getParameter("amountFrom");
        if (amtFromStr != null && !amtFromStr.isBlank()) {
            try { amountFrom = Double.parseDouble(amtFromStr); } catch (NumberFormatException ignored) {}
        }

        Double amountTo = null;
        String amtToStr = request.getParameter("amountTo");
        if (amtToStr != null && !amtToStr.isBlank()) {
            try { amountTo = Double.parseDouble(amtToStr); } catch (NumberFormatException ignored) {}
        }

        if (timeRange == null) {
            timeRange = "all";
        }

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (NumberFormatException ignored) {}
        }
        int pageSize = 10;

        List<Payment> transactions = service.getTransactionsPaging(keyword, type, orderType, paymentMethod, employeeId, amountFrom, amountTo, fromDate, toDate, timeRange, targetBranchId, page, pageSize);
        int totalRecords = service.countTransactions(keyword, type, orderType, paymentMethod, employeeId, amountFrom, amountTo, fromDate, toDate, timeRange, targetBranchId);
        int totalPage = (int) Math.ceil((double) totalRecords / pageSize);

        double totalCash = service.getTotalCashBalance(targetBranchId);
        double totalBank = service.getTotalBankBalance(targetBranchId);
        double totalFund = totalCash + totalBank;

        double cashIncome = service.getSumIncome("CASH", targetBranchId);
        double cashExpense = service.getSumExpense("CASH", targetBranchId);
        double bankIncome = service.getSumIncome("BANK_TRANSFER", targetBranchId);
        double bankExpense = service.getSumExpense("BANK_TRANSFER", targetBranchId);

        List<Map<String, Object>> weeklyStats = service.getWeeklyOverview(keyword, type, paymentMethod, fromDate, toDate, timeRange, targetBranchId);
        double[] weeklyIncome = new double[5];
        double[] weeklyExpense = new double[5];
        for (Map<String, Object> stat : weeklyStats) {
            int weekNum = (int) stat.get("weekNum");
            if (weekNum >= 1 && weekNum <= 5) {
                weeklyIncome[weekNum - 1] = (double) stat.get("totalIncome");
                weeklyExpense[weekNum - 1] = (double) stat.get("totalExpense");
            }
        }

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

        request.setAttribute("transactions", transactions);
        request.setAttribute("totalRecords", totalRecords);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPage", totalPage);

        request.setAttribute("keyword", keyword);
        request.setAttribute("type", type);
        request.setAttribute("orderType", orderType);
        request.setAttribute("paymentMethod", paymentMethod);
        request.setAttribute("fromDate", fromDate);
        request.setAttribute("toDate", toDate);
        request.setAttribute("timeRange", timeRange);
        request.setAttribute("employeeId", employeeId);
        request.setAttribute("amountFrom", amountFrom);
        request.setAttribute("amountTo", amountTo);
        request.setAttribute("selectedBranchId", targetBranchId);
        request.setAttribute("userRole", roleLower);

        request.setAttribute("totalCash", totalCash);
        request.setAttribute("totalBank", totalBank);
        request.setAttribute("totalFund", totalFund);

        request.setAttribute("cashIncome", cashIncome);
        request.setAttribute("cashExpense", cashExpense);
        request.setAttribute("bankIncome", bankIncome);
        request.setAttribute("bankExpense", bankExpense);

        request.setAttribute("chartIncome", incomeStr.toString());
        request.setAttribute("chartExpense", expenseStr.toString());

        List<Order> recentOrders = service.getRecentOrders(50);
        request.setAttribute("recentOrders", recentOrders);

        forward(request, response, "payments/list");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();
        Employee user = (Employee) request.getSession().getAttribute("currentUser");

        if (user == null) {
            request.getSession().setAttribute("message", "Lỗi: Phiên đăng nhập hết hạn.");
            request.getSession().setAttribute("messageType", "danger");
            redirect(response, request.getContextPath() + "/cashbook");
            return;
        }

        String roleLower = (user.getRoleName() != null) ? user.getRoleName().trim().toLowerCase() : "";
        if ("admin".equals(roleLower)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin không có quyền tạo phiếu thu chi.");
            return;
        }

        if ("/cashbook/create-receipt".equals(path) || "/cashbook/create-payment".equals(path)) {
            try {
                double amount = Double.parseDouble(request.getParameter("amount"));
                String method = request.getParameter("method");
                String description = request.getParameter("description");
                String paymentType = "/cashbook/create-receipt".equals(path) ? "INCOME" : "EXPENSE";

                Payment p = new Payment();
                p.setAmount(amount);
                p.setMethod(method);
                p.setDescription(description);
                p.setPaymentType(paymentType);

                String orderIdStr = request.getParameter("orderId");
                if (orderIdStr != null && !orderIdStr.isBlank()) {
                    try {
                        p.setOrderId(Integer.parseInt(orderIdStr));
                    } catch (NumberFormatException ignored) {}
                }

                String error;
                if ("/cashbook/create-receipt".equals(path)) {
                    error = service.createReceipt(p, user.getEmployeeID(), user.getBranchID());
                } else {
                    error = service.createExpense(p, user.getEmployeeID(), user.getBranchID());
                }

                if (error == null) {
                    String msg = paymentType.equals("INCOME") ? "Lập phiếu thu thành công." : "Lập phiếu chi thành công.";
                    request.getSession().setAttribute("message", msg);
                    request.getSession().setAttribute("messageType", "success");
                } else {
                    request.getSession().setAttribute("message", "Lỗi: " + error);
                    request.getSession().setAttribute("messageType", "danger");
                }
            } catch (NumberFormatException e) {
                request.getSession().setAttribute("message", "Lỗi: Số tiền nhập vào không hợp lệ.");
                request.getSession().setAttribute("messageType", "danger");
            } catch (Exception e) {
                request.getSession().setAttribute("message", "Lỗi: " + e.getMessage());
                request.getSession().setAttribute("messageType", "danger");
            }
        }

        redirect(response, request.getContextPath() + "/cashbook");
    }
}
