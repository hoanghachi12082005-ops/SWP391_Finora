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
import java.util.List;

/**
 * Activity Center - Audit Log (immutable).
 * - Chỉ READ ONLY: GET /activity-log để xem nhật ký hệ thống.
 * - Phân quyền: CHỈ role "Owner" mới được truy cập.
 *   - Chưa đăng nhập → redirect /login.
 *   - Đã đăng nhập nhưng không phải Owner → HTTP 403.
 * - KHÔNG cho phép thêm/sửa/xóa: doPost trả về 405 Method Not Allowed.
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

        int page = 1;
        try {
            if (request.getParameter("page") != null)
                page = Integer.parseInt(request.getParameter("page").trim());
        } catch (NumberFormatException ignored) {}

        try {
            int totalCount = dao.countAll(keyword, tableName, actionName);
            int totalPages = (int) Math.ceil((double) totalCount / ITEMS_PER_PAGE);
            page = Math.max(1, Math.min(page, totalPages > 0 ? totalPages : 1));

            List<ActivityLog> logs = dao.findAll(
                    (page - 1) * ITEMS_PER_PAGE, ITEMS_PER_PAGE, keyword, tableName, actionName);

            request.setAttribute("logs", logs);
            request.setAttribute("tables", dao.findDistinctTables());
            request.setAttribute("actions", dao.findDistinctActions());
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalCount", totalCount);
            request.setAttribute("keyword", keyword != null ? keyword : "");
            request.setAttribute("filterTable", tableName != null ? tableName : "");
            request.setAttribute("filterAction", actionName != null ? actionName : "");

            forward(request, response, "activity-log/list");
        } catch (SQLException e) {
            throw new ServletException("Database error retrieving activity logs", e);
        }
    }

    /**
     * Audit log không cho phép ghi đè/xóa từ UI.
     * Mọi POST đều bị từ chối với 405.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Activity log là dữ liệu chỉ đọc. Không thể thêm/sửa/xóa từ giao diện.");
    }

    /**
     * Kiểm tra session + role.
     * @return true nếu được phép tiếp tục, false nếu đã gửi response (redirect/403).
     */
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
}
