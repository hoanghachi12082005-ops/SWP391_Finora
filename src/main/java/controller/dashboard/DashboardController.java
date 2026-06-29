package       controller.dashboard;

import       controller.common.BaseController;
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
import java.util.Collections;
import java.util.List;

@WebServlet(name = "DashboardController", urlPatterns = {"/dashboard/owner", "/dashboard/inventory", "/dashboard/financial"})
public class DashboardController extends BaseController {

    private ActivityLogDAO activityLogDAO;

    @Override
    public void init() throws ServletException {
        activityLogDAO = new ActivityLogDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Card "Hoạt động gần đây" chỉ load dữ liệu khi user là Owner.
        // Vai trò khác sẽ không thấy card này (xem owner.jsp).
        if (isOwner(request)) {
            try {
                List<ActivityLog> recentActivities = activityLogDAO.findRecent(5);
                request.setAttribute("recentActivities", recentActivities);
            } catch (SQLException ex) {
                ex.printStackTrace();
                request.setAttribute("recentActivities", Collections.emptyList());
            }
        } else {
            request.setAttribute("recentActivities", Collections.emptyList());
        }

        String path = request.getServletPath();
        switch (path) {
        case "/dashboard/owner": forward(request, response, "dashboard/owner"); break;
        case "/dashboard/inventory": forward(request, response, "dashboard/inventory"); break;
        case "/dashboard/financial": forward(request, response, "dashboard/financial"); break;
            default: forward(request, response, "dashboard/owner"); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("message", "Đã nhận dữ liệu. Hãy kết nối Service/DAO để xử lý thật.");
        doGet(request, response);
    }

    private boolean isOwner(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object user = (session == null) ? null : session.getAttribute("currentUser");
        if (!(user instanceof Employee)) return false;
        String role = ((Employee) user).getRoleName();
        return role != null && "Owner".equalsIgnoreCase(role.trim());
    }
}
