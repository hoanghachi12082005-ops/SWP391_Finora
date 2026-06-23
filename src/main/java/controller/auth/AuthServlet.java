package controller.auth;

import dao.employee.EmployeeDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Employee;
import service.employee.AuthService;
import util.email.EmailUtil;

@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/logout", "/register", "/forgot-password"})
public class AuthServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private EmployeeDAO employeeDAO;
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        this.employeeDAO = new EmployeeDAO();
        this.authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getServletPath();

        if ("/logout".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        switch (action) {
            case "/login":
                // Views nằm tại webapp/views/ (không có /WEB-INF/)
                request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
                break;
            case "/register":
                request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
                break;
            case "/forgot-password":
                request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getServletPath();

        switch (action) {
            case "/login":
                handleLogin(request, response);
                break;
            case "/register":
                handleRegister(request, response);
                break;
            case "/forgot-password":
                handleForgotPassword(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/login");
                break;
        }
    }

    /**
     * 1. Xử lý Đăng nhập (Login)
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String emailOrPhone = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            // Xác thực thông tin qua tầng xử lý mật khẩu băm AuthService
            Employee employee = authService.login(emailOrPhone, password);

            HttpSession session = request.getSession(true);
            // Lưu user vào session với key "currentUser" — NHẤT QUÁN với toàn bộ hệ thống
            session.setAttribute("currentUser", employee);

            int roleId = employee.getRoleID();
            session.setAttribute("activeRoleId", roleId);

            switch (roleId) {

                case 1: // Admin
                    response.sendRedirect(request.getContextPath() + "/admin/user");
                    break;

                case 2: // Owner
                    response.sendRedirect(request.getContextPath() + "/owner/emp");
                    break;

                case 3: // StoreManager
                    response.sendRedirect(request.getContextPath() + "/manager/emp");
                    break;

                case 4: // SalesStaff
                    response.sendRedirect(request.getContextPath() + "/pos/sale");
                    break;

                case 5: // WarehouseStaff
                    response.sendRedirect(request.getContextPath() + "/warehouse/dashboard");
                    break;

                default:
                    session.invalidate();
                    request.setAttribute("error", "Tài khoản chưa được phân quyền.");
                    request.getRequestDispatcher("/views/auth/login.jsp")
                            .forward(request, response);
                    break;
            }

        } catch (RuntimeException e) {
            System.err.println("[DEBUG] Đăng nhập thất bại do: " + e.getMessage());
            request.setAttribute("error", e.getMessage());
            request.setAttribute("username", emailOrPhone);
            request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
        }
    }

    /**
     * 2. Xử lý Đăng ký cửa hàng & Nhân viên (Register)
     */
    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String shopName = request.getParameter("shopName");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");

        request.setAttribute("shopName", shopName);
        request.setAttribute("fullName", fullName);
        request.setAttribute("email", email);
        request.setAttribute("phone", phone);

        if (fullName == null || email == null || phone == null || password == null
                || fullName.trim().isEmpty()
                || email.trim().isEmpty()
                || phone.trim().isEmpty()
                || password.trim().isEmpty()) {

            request.setAttribute("error", "Vui lòng nhập đầy đủ các thông tin bắt buộc!");
            request.getRequestDispatcher("/views/auth/register.jsp")
                    .forward(request, response);
            return;
        }

        try {

            int defaultRoleId = 2;     // Owner
            int defaultBranchId = -1;  // FIX: Không hardcode branchId, để null → tự gán sau khi tạo store

            boolean isRegistered = authService.register(
                    fullName.trim(),
                    email.trim(),
                    phone.trim(),
                    password.trim(),
                    defaultRoleId,
                    defaultBranchId
            );
            if (isRegistered) {
                request.setAttribute("step", "4");
                request.setAttribute("resShopName", shopName);
                request.setAttribute("resUsername", email.trim());
                // FIX: Không truyền password plaintext ra JSP — rủi ro bảo mật
                request.setAttribute("successMessage",
                        "Đăng ký cửa hàng thành công! Vui lòng đăng nhập bằng email và mật khẩu vừa đặt.");
            } else {
                request.setAttribute("error", "Đăng ký thất bại! Hệ thống đang bận.");
            }
        } catch (RuntimeException e) {
            request.setAttribute("error", e.getMessage());
        }
        request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
    }

    /**
     * 3. Xử lý Quên mật khẩu (Forgot Password)
     */
    private void handleForgotPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");

        request.setAttribute("fullName", fullName);
        request.setAttribute("email", email);

        if (fullName == null || email == null || fullName.trim().isEmpty() || email.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ họ tên và email đăng ký!");
            request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        try {
            boolean isMatch = employeeDAO.checkFullNameAndEmailMatch(fullName.trim(), email.trim());
            if (isMatch) {
                String newPassword = EmailUtil.generateRandomPassword();

                // FIX: Kiểm tra gửi email TRƯỚC khi cập nhật DB để tránh mất quyền truy cập
                boolean sent = EmailUtil.sendPasswordEmail(email.trim(), fullName.trim(), newPassword);
                if (!sent) {
                    request.setAttribute("error", "Không thể gửi email. Vui lòng kiểm tra lại địa chỉ email hoặc thử lại sau!");
                } else {
                    // Chỉ cập nhật DB khi email gửi thành công
                    String hashedPassword = util.security.PasswordUtil.hash(newPassword);
                    boolean updated = employeeDAO.updatePasswordByEmail(email.trim(), hashedPassword);
                    if (updated) {
                        request.setAttribute("successMessage", "Mật khẩu mới đã được gửi thành công qua email của bạn!");
                        request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
                        return;
                    } else {
                        request.setAttribute("error", "Không thể cập nhật mật khẩu mới vào Cơ sở dữ liệu!");
                    }
                }
            } else {
                request.setAttribute("error", "Thông tin Họ tên hoặc Email không khớp với bất kỳ tài khoản nào!");
            }
        } catch (Exception e) {
            request.setAttribute("error", "Có lỗi xảy ra trong quá trình khôi phục: " + e.getMessage());
        }

        request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
    }

    /**
     * 4. Phân luồng trang Quản lý sau Đăng nhập (Role Selection)
     */
}
