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

            // 1. Loại trừ tài nguyên tĩnh và các trang công khai (login/register/logout)
            if (path.startsWith("/assets/")
                    || path.startsWith("/css/")
                    || path.startsWith("/js/")
                    || path.equals("/login")
                    || path.equals("/register")
                    || path.equals("/forgot-password")
                    || path.equals("/logout")) {

                chain.doFilter(request, response);
                return;
            }

            // 2. Kiểm tra trạng thái đăng nhập — dùng key "currentUser" nhất quán
            Employee employee = (session == null) ? null : (Employee) session.getAttribute("currentUser");
            if (employee == null) {
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            // 3. Phân quyền theo vai trò — toLowerCase() nhất quán để so sánh
            String role = employee.getRoleName() == null ? "" : employee.getRoleName().trim().toLowerCase();

            // Chỉ Admin được vào /admin/**
            if (path.startsWith("/admin/")) {
                if (!role.equals("admin")) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Admin only.");
                    return;
                }
            }

            // Admin và Owner được vào /owner/**
            if (path.startsWith("/owner/")) {
                if (!role.equals("admin") && !role.equals("owner")) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Owner or Admin only.");
                    return;
                }
            }

            // Admin, Owner và Store Manager được vào /manager/**
            if (path.startsWith("/manager/")) {
                if (!role.equals("admin") && !role.equals("owner") && !role.equals("store manager")) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Manager only.");
                    return;
                }
            }

            // Admin, Owner, Store Manager được vào /system/** và các trang system
            if (path.startsWith("/system/")
                    || path.equals("/activity-log")
                    || path.equals("/notifications")
                    || path.startsWith("/configuration/")) {
                if (!role.equals("admin") && !role.equals("owner") && !role.equals("store manager")) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
                    return;
                }
            }

            // Admin, Owner, Store Manager, Warehouse Staff được vào /management/**
            if (path.startsWith("/management/")) {
                if (!role.equals("admin")
                        && !role.equals("owner")
                        && !role.equals("store manager")
                        && !role.equals("warehouse staff")) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
                    return;
                }
            }

            // Admin, Owner, Store Manager, Sales Staff được vào /pos/**
            if (path.startsWith("/pos/")) {
                if (!role.equals("admin")
                        && !role.equals("owner")
                        && !role.equals("store manager")
                        && !role.equals("sales staff")) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
                    return;
                }
            }

            // Warehouse Staff và quản lý được vào /warehouse/**
            if (path.startsWith("/warehouse/")) {
                if (!role.equals("admin")
                        && !role.equals("owner")
                        && !role.equals("store manager")
                        && !role.equals("warehouse staff")) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
                    return;
                }
            }

            // Thỏa mãn tất cả điều kiện phân quyền
            chain.doFilter(request, response);
        }
    }
