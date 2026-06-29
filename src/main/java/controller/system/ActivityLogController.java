package controller.system;

import controller.common.BaseController;
import dao.system.ActivityLogDAO;
import model.ActivityLog;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Activity Center: trang quản lý nhật ký hoạt động (audit_log).
 * Hỗ trợ CRUD + phân trang + lọc.
 *  - GET  /activity-log              -> list (kèm filter + phân trang)
 *  - POST /activity-log?action=add   -> thêm
 *  - POST /activity-log?action=edit  -> cập nhật
 *  - POST /activity-log?action=delete-> xóa
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String action = request.getParameter("action");
        if (action == null) action = "";

        try {
            switch (action) {
                case "add": {
                    ActivityLog log = buildFromRequest(request);
                    int newId = dao.insert(log);
                    if (newId > 0) {
                        session.setAttribute("message", "Thêm hoạt động thành công!");
                        session.setAttribute("messageType", "success");
                    } else {
                        session.setAttribute("message", "Không thêm được hoạt động.");
                        session.setAttribute("messageType", "danger");
                    }
                    break;
                }
                case "edit": {
                    ActivityLog log = buildFromRequest(request);
                    log.setId(Integer.parseInt(request.getParameter("id")));
                    boolean ok = dao.update(log);
                    session.setAttribute("message", ok ? "Cập nhật hoạt động thành công!" : "Không cập nhật được.");
                    session.setAttribute("messageType", ok ? "success" : "danger");
                    break;
                }
                case "delete": {
                    int id = Integer.parseInt(request.getParameter("id"));
                    boolean ok = dao.delete(id);
                    session.setAttribute("message", ok ? "Xóa hoạt động thành công!" : "Không xóa được.");
                    session.setAttribute("messageType", ok ? "success" : "danger");
                    break;
                }
                default:
                    session.setAttribute("message", "Hành động không hợp lệ.");
                    session.setAttribute("messageType", "warning");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            session.setAttribute("message", "Lỗi xử lý: " + ex.getMessage());
            session.setAttribute("messageType", "danger");
        }

        response.sendRedirect(buildRedirectUrl(request));
    }

    private ActivityLog buildFromRequest(HttpServletRequest request) {
        ActivityLog log = new ActivityLog();
        String empIdStr = request.getParameter("empId");
        if (empIdStr != null && !empIdStr.isBlank()) {
            try { log.setEmpId(Integer.parseInt(empIdStr.trim())); } catch (NumberFormatException ignored) {}
        }
        log.setActionName(trimToNull(request.getParameter("actionName")));
        log.setTableName(trimToNull(request.getParameter("tableName")));
        String recordIdStr = request.getParameter("recordId");
        if (recordIdStr != null && !recordIdStr.isBlank()) {
            try { log.setRecordId(Integer.parseInt(recordIdStr.trim())); } catch (NumberFormatException ignored) {}
        }
        log.setOldData(trimToNull(request.getParameter("oldData")));
        log.setNewData(trimToNull(request.getParameter("newData")));
        return log;
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private String buildRedirectUrl(HttpServletRequest request) {
        String keyword = request.getParameter("keyword");
        String tableName = request.getParameter("tableName");
        String actionName = request.getParameter("actionName");
        String page = request.getParameter("page");
        StringBuilder sb = new StringBuilder(request.getContextPath() + "/activity-log?");
        if (keyword != null && !keyword.isBlank()) sb.append("keyword=").append(keyword).append("&");
        if (tableName != null && !tableName.isBlank()) sb.append("tableName=").append(tableName).append("&");
        if (actionName != null && !actionName.isBlank()) sb.append("actionName=").append(actionName).append("&");
        if (page != null && !page.isBlank()) sb.append("page=").append(page);
        char last = sb.charAt(sb.length() - 1);
        if (last == '&' || last == '?') sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
