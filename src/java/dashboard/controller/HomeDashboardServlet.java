package dashboard.controller;

import common.dto.ModuleDTO;
import common.util.ModuleRegistry;
import common.util.RoleContextUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** Development home dashboard. Login is intentionally not the first screen. */
public class HomeDashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String currentRole = RoleContextUtil.getCurrentRole(request);
        List<ModuleDTO> modules = ModuleRegistry.getModules(currentRole);
        request.setAttribute("currentRole", currentRole);
        request.setAttribute("modules", modules);
        request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
    }
}
