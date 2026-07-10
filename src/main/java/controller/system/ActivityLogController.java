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
 * Trung tâm hoạt động - Audit Log (immutable, read-only).
 * - Chỉ GET; doPost trả 405.
 * - Chỉ Owner mới được truy cập; chưa login → /login; không phải Owner → 403.
 *
 * Trả về cho JSP các bộ filter dạng "value → nhãn nghiệp vụ" để Owner đọc dễ:
 *   - entityOptions   : value = table_name kỹ thuật, label = "Đơn hàng", "Sản phẩm"...
 *   - actionOptions   : value = action_name kỹ thuật, label = "Tạo mới", "Cập nhật"...
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

        if (!ensureOwner(request, response)) return;

        String keyword = request.getParameter("keyword");
        String tableName = request.getParameter("tableName");
        String actionName = request.getParameter("actionName");
        String dateFromRaw = request.getParameter("dateFrom");
        String dateToRaw   = request.getParameter("dateTo");

        LocalDate dateFrom = parseDate(dateFromRaw);
        LocalDate dateTo   = parseDate(dateToRaw);
        // Nếu khoảng đảo ngược (from > to) thì hoán đổi để tránh kết quả rỗng vô lý.
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            LocalDate tmp = dateFrom; dateFrom = dateTo; dateTo = tmp;
        }

        int page = 1;
        try {
            if (request.getParameter("page") != null)
                page = Integer.parseInt(request.getParameter("page").trim());
        } catch (NumberFormatException ignored) {}

        // Thiết lập các bảng log được phép hiển thị (giới hạn activity center đúng 4 nhóm log cần thiết)
        java.util.List<String> allowedTableNames = java.util.Arrays.asList(
            "order", "orders", "order_detail",    // bán hàng
            "product",                              // thêm/sửa/xóa sản phẩm  
            "inventory", "stock_transaction", "stock_transfer", // kho
            "branch", "store"                      // chi nhánh
        );
        // Nếu filter table được cung cấp, giữ lại nếu thuộc danh sách cho phép
        if (tableName != null && !allowedTableNames.contains(tableName.toLowerCase())) {
            tableName = null; // vô hiệu hóa filter table không được phép
        }

        try {
            int totalCount = tableName != null ? 
                dao.countByTableName(keyword, tableName, actionName, dateFrom, dateTo) :
                dao.countAll(keyword, tableName, actionName, dateFrom, dateTo);
            int totalPages = (int) Math.ceil((double) totalCount / ITEMS_PER_PAGE);
            page = Math.max(1, Math.min(page, totalPages > 0 ? totalPages : 1));

            List<ActivityLog> logs = dao.findAll(
                    (page - 1) * ITEMS_PER_PAGE, ITEMS_PER_PAGE,
                    keyword, tableName, actionName, dateFrom, dateTo);

            // Chỉ hiển thị các bảng được phép trong filter dropdown
            request.setAttribute("entityOptions", buildEntityOptionsFiltered(dao.findDistinctTables(), allowedTableNames));
            request.setAttribute("actionOptions", buildActionOptions(dao.findDistinctActions()));

            request.setAttribute("logs", logs);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
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

    private boolean ensureOwner(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        Object user = (session == null) ? null : session.getAttribute("currentUser");
        if (!(user instanceof Employee)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        Employee emp = (Employee) user;
        String role = emp.getRoleName();
        if (role == null || !"Owner".equalsIgnoreCase(role.trim())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền xem Activity Log. Chỉ Owner mới được truy cập.");
            return false;
        }
        return true;
    }

    /** Parse "yyyy-MM-dd" từ input HTML date; trả null nếu trống/không hợp lệ. */
    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /** Sinh map { table_name → "Đối tượng nghiệp vụ" } để render dropdown. */
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

    /** Sinh map { action_name → "Loại thao tác" } để render dropdown. */
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
