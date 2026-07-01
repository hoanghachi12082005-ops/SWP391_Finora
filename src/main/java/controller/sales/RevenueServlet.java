package controller.sales;

import dao.sales.RevenueDAO;
import dao.sales.EmployeeDAO;
import model.RevenueSummary;
import model.Employee;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/revenue")
public class RevenueServlet extends HttpServlet {

    private final RevenueDAO revenueDao = new RevenueDAO();
    private final EmployeeDAO employeeDao = new EmployeeDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        req.setAttribute("activePage", "revenue");

        HttpSession session = req.getSession();
        Employee emp = (Employee) session.getAttribute("employee");
        if (emp == null) {
            emp = new Employee();
            emp.setEmpId(1);
            emp.setBranchId(1);
            emp.setFullName("Thu ngân #1");
            session.setAttribute("employee", emp);
        }

        int branchId = emp.getBranchId();

        // 1. Parse Filters
        String dateStr = req.getParameter("date");
        LocalDate date = LocalDate.now();
        if (dateStr != null && !dateStr.isBlank()) {
            try {
                date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                // Keep default today
            }
        }

        String empIdStr = req.getParameter("empId");
        int empId = 0;
        if (empIdStr != null && !empIdStr.isBlank()) {
            try {
                empId = Integer.parseInt(empIdStr);
            } catch (NumberFormatException ignored) {}
        }

        // 2. Fetch Employee List for Dropdown
        List<Employee> employeeList = new ArrayList<>();
        try {
            employeeList = employeeDao.getByBranch(branchId);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 3. Fetch KPI Summary
        RevenueSummary kpi = revenueDao.getKpiSummary(branchId, empId, date);

        // 4. Fetch Hourly Data
        Map<Integer, Double[]> hourlyMap = revenueDao.getRevenueByHour(branchId, empId, date);
        List<String> labels = new ArrayList<>();
        List<Double> todayVals = new ArrayList<>();
        List<Double> yesterdayVals = new ArrayList<>();
        for (int h = 8; h <= 22; h++) {
            labels.add(String.format("%02d:00", h));
            Double[] vals = hourlyMap.get(h);
            todayVals.add(vals != null ? vals[0] : 0.0);
            yesterdayVals.add(vals != null ? vals[1] : 0.0);
        }

        // 5. Fetch Payment Method Breakdown
        Map<String, Double> paymentBreakdown = revenueDao.getPaymentMethodBreakdown(branchId, empId, date);
        double cashTotal = paymentBreakdown.getOrDefault("CASH", 0.0);
        double bankTotal = paymentBreakdown.getOrDefault("BANK_TRANSFER", 0.0);
        double overallPaymentTotal = cashTotal + bankTotal;
        double cashPct = overallPaymentTotal > 0 ? (cashTotal / overallPaymentTotal) * 100 : 0;
        double bankPct = overallPaymentTotal > 0 ? (bankTotal / overallPaymentTotal) * 100 : 0;

        // 6. Fetch Top Selling Products
        List<Map<String, Object>> topProducts = revenueDao.getTopSellingProducts(branchId, empId, date, 5);

        // 7. Fetch Recent Transactions
        List<Map<String, Object>> recentTransactions = revenueDao.getRecentTransactions(branchId, empId, 5);

        // Set request attributes
        req.setAttribute("selectedDate", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        req.setAttribute("selectedDateFormatted", date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        req.setAttribute("selectedEmpId", empId);
        req.setAttribute("employeeList", employeeList);
        req.setAttribute("kpi", kpi);

        req.setAttribute("hourlyLabels", labels);
        req.setAttribute("hourlyToday", todayVals);
        req.setAttribute("hourlyYesterday", yesterdayVals);

        req.setAttribute("cashTotal", cashTotal);
        req.setAttribute("bankTotal", bankTotal);
        req.setAttribute("cashPct", cashPct);
        req.setAttribute("bankPct", bankPct);

        req.setAttribute("topProducts", topProducts);
        req.setAttribute("recentTransactions", recentTransactions);

        req.getRequestDispatcher("/views/sales/revenue.jsp").forward(req, resp);
    }
}
