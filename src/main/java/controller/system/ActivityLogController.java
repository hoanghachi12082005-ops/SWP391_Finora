package controller.system;

import controller.common.BaseController;
import dao.system.ActivityLogDAO;
import model.ActivityLog;
import model.Employee;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Trung tâm hoạt động — Audit Log.
 * Dùng page number (1,2,3...) nhưng backend dùng keyset để query nhanh.
 * - Chỉ GET; doPost trả 405.
 * - Chỉ Owner/Admin mới được truy cập.
 */
@WebServlet(name = "ActivityLogController", urlPatterns = {"/activity-log"})
public class ActivityLogController extends BaseController {

    private static final int ITEMS_PER_PAGE = 10;
    private ActivityLogDAO dao;

    @Override
    public void init() throws ServletException {
        dao = new ActivityLogDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!ensureAdmin(request, response)) return;

        String keyword    = request.getParameter("keyword");
        String tableName  = request.getParameter("tableName");
        String actionName = request.getParameter("actionName");
        String dateFromRaw = request.getParameter("dateFrom");
        String dateToRaw   = request.getParameter("dateTo");

        LocalDate dateFrom = parseDate(dateFromRaw);
        LocalDate dateTo   = parseDate(dateToRaw);
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            LocalDate tmp = dateFrom; dateFrom = dateTo; dateTo = tmp;
        }

        // Page number
        int currentPage = 1;
        try {
            String p = request.getParameter("page");
            if (p != null && !p.isBlank()) currentPage = Math.max(1, Integer.parseInt(p.trim()));
        } catch (NumberFormatException ignored) {}

        // Giới hạn bảng được phép hiển thị
        List<String> allowedTableNames = java.util.Arrays.asList(
            "order", "orders", "order_detail",
            "product",
            "inventory", "stock_transaction", "stock_transfer",
            "branch", "store"
        );
        if (tableName != null && !allowedTableNames.contains(tableName.toLowerCase())) {
            tableName = null;
        }

        try {
            // Keyset-style query theo page number
            List<ActivityLog> logs = dao.findByPage(
                    currentPage, ITEMS_PER_PAGE + 1,
                    keyword, tableName, actionName, dateFrom, dateTo);

            boolean hasNext = false;
            if (logs.size() > ITEMS_PER_PAGE) {
                hasNext = true;
                logs.remove(logs.size() - 1);
            }

            // Tổng số → tính totalPages
            int totalCount = tableName != null
                    ? dao.countByTableName(keyword, tableName, actionName, dateFrom, dateTo)
                    : dao.countAll(keyword, tableName, actionName, dateFrom, dateTo);
            int totalPages = (int) Math.ceil((double) totalCount / ITEMS_PER_PAGE);
            if (totalPages < 1) totalPages = 1;

            request.setAttribute("entityOptions", buildEntityOptionsFiltered(dao.findDistinctTables(), allowedTableNames));
            request.setAttribute("actionOptions", buildActionOptions(dao.findDistinctActions()));

            request.setAttribute("logs", logs);
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalCount", totalCount);
            request.setAttribute("hasNext", hasNext);
            request.setAttribute("keyword", keyword != null ? keyword : "");
            request.setAttribute("filterTable", tableName != null ? tableName : "");
            request.setAttribute("filterAction", actionName != null ? actionName : "");
            request.setAttribute("filterDateFrom", dateFrom != null ? dateFrom.toString() : "");
            request.setAttribute("filterDateTo",   dateTo != null ? dateTo.toString() : "");

            forward(request, response, "activity-log/list");
        } catch (SQLException e) {
            throw new ServletException("Database error retrieving activity logs", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Activity log là dữ liệu chỉ đọc. Không thể thêm/sửa/xóa từ giao diện.");
    }

    private boolean ensureAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        Object user = (session == null) ? null : session.getAttribute("currentUser");
        if (!(user instanceof Employee)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        Employee emp = (Employee) user;
        String role = emp.getRoleName();
        if (role == null || (!"Owner".equalsIgnoreCase(role.trim()) && !"Admin".equalsIgnoreCase(role.trim()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền xem Activity Log. Chỉ Owner và Admin mới được truy cập.");
            return false;
        }
        return true;
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private Map<String, String> buildEntityOptions(List<String> tables) {
        Map<String, String> options = new LinkedHashMap<>();
        if (tables == null) return options;
        for (String t : tables) {
            ActivityLog tmp = new ActivityLog();
            tmp.setTableName(t);
            options.put(t, tmp.getEntityLabel());
        }
        return options;
    }

    private Map<String, String> buildEntityOptionsFiltered(List<String> tables, List<String> allowed) {
        Map<String, String> options = new LinkedHashMap<>();
        if (tables == null) return options;
        for (String t : tables) {
            if (allowed.contains(t.toLowerCase())) {
                ActivityLog tmp = new ActivityLog();
                tmp.setTableName(t);
                options.put(t, tmp.getEntityLabel());
            }
        }
        return options;
    }

    private Map<String, String> buildActionOptions(List<String> actions) {
        Map<String, String> options = new LinkedHashMap<>();
        if (actions == null) return options;
        for (String a : actions) {
            ActivityLog tmp = new ActivityLog();
            tmp.setActionName(a);
            options.put(a, tmp.getActionLabel());
        }
        return options;
    }
}
