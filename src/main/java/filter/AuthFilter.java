package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Employee;
import java.io.IOException;

@WebFilter(filterName = "AuthFilter", urlPatterns = {"/*"}) 
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String path = req.getRequestURI().substring(req.getContextPath().length());

        // 1. Loại trừ các tài nguyên tĩnh và các trang login/register/logout công khai
        if (path.startsWith("/assets/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.equals("/login")
                || path.equals("/register")
                || path.equals("/forgot-password")
                || path.equals("/logout")
                || path.equals("/role-selection")) {

            chain.doFilter(request, response);
            return;
        }

        // 2. Kiểm tra trạng thái đăng nhập (Đồng bộ key "currentEmployee")
        Employee employee = (session == null) ? null : (Employee) session.getAttribute("currentUser");
        if (employee == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 3. Phân quyền chi tiết dựa trên vai trò (Role Name) cấu hình trong Hệ thống FinoraRetail
        String role = employee.getRoleName().trim().toLowerCase();

        if (path.startsWith("/system/")) {
            if (!role.equals("admin")
                    && !role.equals("owner")) {

                resp.sendError(403);
                return;
            }
        }
        
        if (path.startsWith("/management/")) {
            if (!role.equals("admin")
                    && !role.equals("owner")
                    && !role.equals("storemanager")
                    && !role.equals("warehousestaff")) {

                resp.sendError(403);
                return;
            }
        }
        
        if (path.startsWith("/pos/")) {
            if (!role.equals("admin")
                    && !role.equals("owner")
                    && !role.equals("storemanager")
                    && !role.equals("salesstaff")) {

                resp.sendError(403);
                return;
            }
        }
        
        if (path.startsWith("/report/")) {
            chain.doFilter(request, response);
            return;
        }

        // Thỏa mãn tất cả các điều kiện phân quyền
        chain.doFilter(request, response);
    }
}
