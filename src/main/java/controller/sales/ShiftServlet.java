package controller.sales;

import dao.sales.ShiftDAO;
import dao.sales.CashTransactionDAO;
import model.Shift;
import model.CashTransaction;
import model.Employee;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@WebServlet("/shift")
public class ShiftServlet extends HttpServlet {

    private final ShiftDAO shiftDao = new ShiftDAO();
    private final CashTransactionDAO cashTxDao = new CashTransactionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        req.setAttribute("activePage", "shift");

        HttpSession session = req.getSession();
        Employee emp = (Employee) session.getAttribute("employee");
        if (emp == null) {
            emp = new Employee();
            emp.setEmpId(1);
            emp.setBranchId(1);
            emp.setFullName("Thu ngân #1");
            session.setAttribute("employee", emp);
        }

        int empId = emp.getEmpId();
        int branchId = emp.getBranchId();

        // Check if there is an active (open) shift for this employee
        Shift activeShift = shiftDao.getOpenShiftByEmp(empId);
        
        if (activeShift != null) {
            // Load shift summary and transactions
            Map<String, Object> summary = shiftDao.getShiftSummary(activeShift.getShiftId());
            req.setAttribute("shiftSummary", summary);

            List<CashTransaction> transactions = cashTxDao.getByShiftId(activeShift.getShiftId());
            req.setAttribute("transactions", transactions);
            req.setAttribute("activeShift", activeShift);
        } else {
            // Load shift history
            List<Shift> history = shiftDao.getShiftHistory(branchId, 10);
            req.setAttribute("shiftHistory", history);
        }

        req.getRequestDispatcher("/views/sales/shift.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        Employee emp = (Employee) session.getAttribute("employee");
        if (emp == null) {
            resp.sendRedirect(req.getContextPath() + "/shift?error=unauthorized");
            return;
        }

        String action = req.getParameter("action");
        if ("open".equals(action)) {
            handleOpenShift(req, resp, emp);
        } else if ("close".equals(action)) {
            handleCloseShift(req, resp, emp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/shift");
        }
    }

    private void handleOpenShift(HttpServletRequest req, HttpServletResponse resp, Employee emp) throws IOException {
        String openingCashStr = req.getParameter("openingCash");
        BigDecimal openingCash = BigDecimal.ZERO;
        if (openingCashStr != null && !openingCashStr.isBlank()) {
            try {
                openingCash = new BigDecimal(openingCashStr.replaceAll("[^\\d.]", ""));
            } catch (Exception ignored) {}
        }

        // Prevent opening if there's already an active shift
        Shift activeShift = shiftDao.getOpenShiftByEmp(emp.getEmpId());
        if (activeShift == null) {
            shiftDao.openShift(emp.getEmpId(), emp.getBranchId(), openingCash);
        }
        resp.sendRedirect(req.getContextPath() + "/shift");
    }

    private void handleCloseShift(HttpServletRequest req, HttpServletResponse resp, Employee emp) throws IOException {
        String closingCashStr = req.getParameter("closingCash");
        BigDecimal closingCash = BigDecimal.ZERO;
        if (closingCashStr != null && !closingCashStr.isBlank()) {
            try {
                closingCash = new BigDecimal(closingCashStr.replaceAll("[^\\d]", ""));
            } catch (Exception ignored) {}
        }

        Shift activeShift = shiftDao.getOpenShiftByEmp(emp.getEmpId());
        if (activeShift == null) {
            resp.sendRedirect(req.getContextPath() + "/shift?error=no_active_shift");
            return;
        }

        // Validate: không để trống hoặc = 0
        if (closingCash.compareTo(BigDecimal.ZERO) <= 0) {
            resp.sendRedirect(req.getContextPath() + "/shift?error=empty_closing_cash");
            return;
        }

        BigDecimal expectedCash = shiftDao.getExpectedCash(activeShift.getShiftId());
        boolean hasDiscrepancy = closingCash.compareTo(expectedCash) != 0;
        String closingNote = req.getParameter("closingNote");

        // Nếu lệch và chưa có lý do → yêu cầu nhập lý do
        if (hasDiscrepancy && (closingNote == null || closingNote.trim().isEmpty())) {
            resp.sendRedirect(req.getContextPath() + "/shift?error=need_reason"
                + "&expected=" + expectedCash.toPlainString()
                + "&closing=" + closingCash.toPlainString());
            return;
        }

        boolean closed = shiftDao.closeShift(activeShift.getShiftId(), closingCash, closingNote);
        if (!closed) {
            resp.sendRedirect(req.getContextPath() + "/shift?error=close_failed");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/shift?success=closed");
    }
}
