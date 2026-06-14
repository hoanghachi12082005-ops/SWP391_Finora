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

@WebFilter(filterName = "AuthFilter", urlPatterns = {"/*"}) // Cấu hình quét toàn hệ thống
public class AuthFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String path = req.getRequestURI().substring(req.getContextPath().length());

        // 1. Loại trừ các tài nguyên tĩnh và các trang login/register/logout công khai
        if (path.startsWith("/assets/") || path.startsWith("/css/") || path.startsWith("/js/") ||
            path.equals("/login") || path.equals("/register") || path.equals("/forgot-password") || path.equals("/logout")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Kiểm tra trạng thái đăng nhập (Đồng bộ key "currentEmployee")
        Employee employee = (session == null) ? null : (Employee) session.getAttribute("currentEmployee");
        if (employee == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 3. Phân quyền chi tiết dựa trên vai trò (Role Name) cấu hình trong Hệ thống FinoraRetail
        String role = employee.getRoleName() != null ? employee.getRoleName().toLowerCase() : "";

        if (path.startsWith("/system/") || path.startsWith("/configuration/")) {
            // Chỉ duy nhất hệ thống cấp Admin mới có quyền cấu hình core
            if (!role.equals("admin")) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập cấu hình hệ thống.");
                return;
            }
        } 
        else if (path.startsWith("/management/")) {
            // Owner quản lý toàn chuỗi, Manager quản lý tại 1 chi nhánh được phép vào
            if (!role.equals("admin") && !role.equals("owner") && !role.equals("manager")) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang quản lý.");
                return;
            }
        } 
        else if (path.startsWith("/pos/") || path.startsWith("/sale")) {
            // Nhân viên bán hàng (Sales Staff) và các cấp quản lý được vào trang bán hàng
            if (!role.equals("admin") && !role.equals("owner") && !role.equals("manager") && !role.equals("sales staff")) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập chức năng bán hàng.");
                return;
            }
        } 
        else if (path.startsWith("/inventory/") || path.startsWith("/purchase-orders/")) {
            // Nhân viên kho (Warehouse) chịu trách nhiệm vận hành khu vực này
            if (!role.equals("admin") && !role.equals("owner") && !role.equals("warehouse")) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập kho hàng.");
                return;
            }
        }

        // Thỏa mãn tất cả các điều kiện phân quyền
        chain.doFilter(request, response);
    }
}