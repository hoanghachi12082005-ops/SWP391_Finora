package controller;

import dto.ModuleDTO;
import util.ModuleRegistry;
import util.RoleContextUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/** Generic controller for module workspaces whose business workflow is not implemented yet. */
public class SkeletonModuleServlet extends HttpServlet {
    private static final Map<String, String> ROUTE_ALIASES = Map.ofEntries(
            Map.entry("/authorization", "/employee-management"),
            Map.entry("/warehouse-management", "/inventory-management"),
            Map.entry("/stock-transfer", "/inventory-management"),
            Map.entry("/order-management", "/sales-management"),
            Map.entry("/payment-management", "/sales-management"),
            Map.entry("/invoice-management", "/sales-management"),
            Map.entry("/website-management", "/website"),
            Map.entry("/seo-management", "/website"),
            Map.entry("/audit-log", "/system-configuration"),
            Map.entry("/notifications", "/system-configuration")
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String route = normalizeModuleRoute(request.getServletPath());
        String currentRole = RoleContextUtil.getCurrentRole(request);
        ModuleDTO module = ModuleRegistry.findModuleByRoute(route, currentRole);

        request.setAttribute("currentRole", currentRole);
        request.setAttribute("module", module);
        request.getRequestDispatcher("/WEB-INF/views/common/skeleton-page.jsp").forward(request, response);
    }

    private String normalizeModuleRoute(String servletPath) {
        return ROUTE_ALIASES.getOrDefault(servletPath, servletPath);
    }
}
