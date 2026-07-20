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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Trung tâm hoạt động — Audit Log.
 * Dùng Keyset Pagination thuần (before/after by audit_log_id).
 * Limit = 20 / trang.
 * Chỉ Owner/Admin mới được truy cập.
 */
@WebServlet(name = "ActivityLogController", urlPatterns = {"/activity-log"})
public class ActivityLogController extends BaseController {

    private static final int ITEMS_PER_PAGE = 20;
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

        // Keyset: before → cũ hơn, after → mới hơn
        Integer beforeId = null;
        Integer afterId  = null;
        try {
            String b = request.getParameter("before");
            String a = request.getParameter("after");
            if (b != null && !b.isBlank()) beforeId = Integer.parseInt(b.trim());
            if (a != null && !a.isBlank()) afterId  = Integer.parseInt(a.trim());
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
            // Lấy ITEMS_PER_PAGE + 1 để biết còn trang tiếp không
            List<ActivityLog> logs = dao.findByKeyset(
                    beforeId, afterId, ITEMS_PER_PAGE + 1,
                    keyword, tableName, actionName, dateFrom, dateTo);

            boolean hasNext = false;
            boolean hasPrev = false;
            if (logs.isEmpty()) {
                // Trường hợp empty: after/before dẫn tới ko có data
                // Fallback về trang đầu
                response.sendRedirect(request.getContextPath() + "/activity-log"
                    + buildFilterQueryString(keyword, tableName, actionName,
                        dateFrom != null ? dateFrom.toString() : null,
                        dateTo != null ? dateTo.toString() : null, null, null));
                return;
            }

            if (logs.size() > ITEMS_PER_PAGE) {
                hasNext = true;
                logs.remove(logs.size() - 1);
            }

            int firstId = logs.get(0).getId();
            int lastId  = logs.get(logs.size() - 1).getId();

            // hasPrev: có bản ghi mới hơn không?
            if (beforeId != null) {
                // Đã đi "cũ hơn" → chắc chắn có mới hơn
                hasPrev = true;
            } else if (afterId != null) {
                // Đã đi "mới hơn" → kiểm tra xem còn bản ghi nào mới hơn firstId không
                hasPrev = dao.existsGreaterThan(firstId, keyword, tableName, actionName, dateFrom, dateTo);
            }
            // else: trang đầu (cả before và after đều null → hasPrev = false)

            // Tổng số — chỉ để hiển thị
            int totalCount = tableName != null
                    ? dao.countByTableName(keyword, tableName, actionName, dateFrom, dateTo)
                    : dao.countAll(keyword, tableName, actionName, dateFrom, dateTo);

            request.setAttribute("entityOptions", buildEntityOptionsFiltered(dao.findDistinctTables(), allowedTableNames));
            request.setAttribute("actionOptions", buildActionOptions(dao.findDistinctActions()));

            request.setAttribute("logs", logs);
            request.setAttribute("hasNext", hasNext);
            request.setAttribute("hasPrev", hasPrev);
            request.setAttribute("firstId", firstId);
            request.setAttribute("lastId", lastId);
            request.setAttribute("totalCount", totalCount);
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

    /** Build query string giữ lại filter params khi redirect về trang đầu. */
    private String buildFilterQueryString(String keyword, String tableName, String actionName,
                                           String dateFrom, String dateTo,
                                           String before, String after) {
        StringBuilder qs = new StringBuilder();
        try {
            if (keyword != null && !keyword.isBlank()) qs.append("&keyword=").append(URLEncoder.encode(keyword, "UTF-8"));
            if (tableName != null && !tableName.isBlank()) qs.append("&tableName=").append(URLEncoder.encode(tableName, "UTF-8"));
            if (actionName != null && !actionName.isBlank()) qs.append("&actionName=").append(URLEncoder.encode(actionName, "UTF-8"));
            if (dateFrom != null && !dateFrom.isBlank()) qs.append("&dateFrom=").append(URLEncoder.encode(dateFrom, "UTF-8"));
            if (dateTo != null && !dateTo.isBlank()) qs.append("&dateTo=").append(URLEncoder.encode(dateTo, "UTF-8"));
            if (before != null) qs.append("&before=").append(before);
            if (after != null) qs.append("&after=").append(after);
        } catch (UnsupportedEncodingException ignored) {}
        if (qs.length() > 0) qs.replace(0, 1, "?");
        return qs.toString();
    }
}
