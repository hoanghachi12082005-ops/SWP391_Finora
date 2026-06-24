package       controller.dashboard;

import       controller.common.BaseController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "DashboardController", urlPatterns = {"/dashboard/owner", "/dashboard/inventory", "/dashboard/financial"})
public class DashboardController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
}
