package controller.sales;

import dao.sales.ShiftDAO;
import dao.sales.CashTransactionDAO;
import model.Shift;
import model.Employee;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

@WebServlet("/shift/cash")
public class CashTransactionServlet extends HttpServlet {

    private final ShiftDAO shiftDao = new ShiftDAO();
    private final CashTransactionDAO cashTxDao = new CashTransactionDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession();
        Employee emp = (Employee) session.getAttribute("employee");
        if (emp == null) {
            out.write("{\"status\":\"error\",\"message\":\"Chưa đăng nhập.\"}");
            return;
        }

        Shift activeShift = shiftDao.getOpenShiftByEmp(emp.getEmpId());
        if (activeShift == null) {
            out.write("{\"status\":\"error\",\"message\":\"Không tìm thấy ca làm việc đang mở.\"}");
            return;
        }

        String type = req.getParameter("type");
        if (!"WITHDRAW".equals(type) && !"DEPOSIT".equals(type)) {
            out.write("{\"status\":\"error\",\"message\":\"Loại giao dịch không hợp lệ.\"}");
            return;
        }

        String amountStr = req.getParameter("amount");
        BigDecimal amount = BigDecimal.ZERO;
        try {
            if (amountStr != null) {
                amountStr = amountStr.replaceAll("[^\\d.]", "");
                amount = new BigDecimal(amountStr);
            }
        } catch (Exception e) {
            out.write("{\"status\":\"error\",\"message\":\"Số tiền không hợp lệ.\"}");
            return;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            out.write("{\"status\":\"error\",\"message\":\"Số tiền phải lớn hơn 0.\"}");
            return;
        }

        // Prevent withdraw exceeding current cash balance
        if ("WITHDRAW".equals(type)) {
            BigDecimal expectedCash = shiftDao.getExpectedCash(activeShift.getShiftId());
            if (amount.compareTo(expectedCash) > 0) {
                String msg = "Số tiền rút vượt quá số dư hiện có trong két ("
                    + expectedCash.setScale(0, java.math.RoundingMode.HALF_UP) + "đ).";
                out.write("{\"status\":\"error\",\"message\":\"" + msg + "\"}");
                return;
            }
        }

        // Idempotency check: prevent duplicate within 5 seconds
        if (cashTxDao.hasRecentDuplicate(activeShift.getShiftId(), type, amount, 5)) {
            out.write("{\"status\":\"error\",\"message\":\"Giao dịch trùng lặp, vui lòng chờ và thử lại.\"}");
            return;
        }

        String note = req.getParameter("note");
        if (note != null) {
            note = note.trim();
        }

        // Insert cash transaction
        boolean success = cashTxDao.insert(activeShift.getShiftId(), type, amount, note);
        if (success) {
            // Recalculate expected cash
            BigDecimal expectedCash = shiftDao.getExpectedCash(activeShift.getShiftId());
            
            out.write("{\"status\":\"success\",\"expected_cash\":" + expectedCash + "}");
        } else {
            out.write("{\"status\":\"error\",\"message\":\"Lỗi hệ thống khi thực hiện giao dịch.\"}");
        }
    }
}
