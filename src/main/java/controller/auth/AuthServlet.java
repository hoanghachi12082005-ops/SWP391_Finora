package controller.auth;
import dao.employee.EmployeeDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import model.Employee;
import service.employee.AuthService;
import util.email.EmailUtil;
import java.security.SecureRandom;
import java.util.Base64;
@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/logout", "/forgot-password", "/role-selection"})
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
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("remembered_username".equals(cookie.getName())) {
                            request.setAttribute("username", cookie.getValue());
                            request.setAttribute("rememberMe", true);
                            break;
                        }
                    }
                }
                request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
                break;
            

            case "/forgot-password":
                request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
                break;
            case "/role-selection":
                HttpSession session = request.getSession(false);
                if (session == null || session.getAttribute("currentUser") == null) {
                    response.sendRedirect(request.getContextPath() + "/login");
                    return;
                }
                Employee emp = (Employee) session.getAttribute("currentUser");
                response.sendRedirect(request.getContextPath() + getRedirectPath(emp));
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
            
            case "/forgot-password":
                handleForgotPassword(request, response);
                break;
            case "/role-selection":
                handleRoleSelection(request, response);
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
        if (emailOrPhone == null || password == null || emailOrPhone.trim().isEmpty() || password.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập email/số điện thoại và mật khẩu.");
            request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
            return;
        }
        try {
            Employee employee = authService.login(emailOrPhone.trim(), password);

            // Ghi nhớ đăng nhập
            String rememberMe = request.getParameter("remember-me");
            Cookie rememberCookie = new Cookie("remembered_username", emailOrPhone.trim());
            if (rememberMe != null) {
                rememberCookie.setMaxAge(30 * 24 * 60 * 60);
            } else {
                rememberCookie.setMaxAge(0);
            }
            rememberCookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
            response.addCookie(rememberCookie);

            // Session fixation protection: regenerate session ID
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) oldSession.invalidate();
            HttpSession session = request.getSession(true);

            session.setAttribute("currentUser", employee);
            session.setAttribute("employee", employee);

            // Generate CSRF token
            byte[] csrfBytes = new byte[32];
            new SecureRandom().nextBytes(csrfBytes);
            session.setAttribute("csrfToken", Base64.getEncoder().encodeToString(csrfBytes));

            response.sendRedirect(request.getContextPath() + getRedirectPath(employee));
        } catch (RuntimeException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("username", emailOrPhone);
            request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
        }
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
                String hashedPassword = util.security.PasswordUtil.hash(newPassword);
                boolean sent = EmailUtil.sendPasswordEmail(email.trim(), fullName.trim(), newPassword);
                if (sent) {
                    boolean updated = employeeDAO.updatePasswordByEmail(email.trim(), hashedPassword);
                    if (updated) {
                        request.setAttribute("successMessage", "Password đã được gửi về email của bạn, vui lòng check mail và đăng nhập lại.");
                        request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
                        return;
                    }
                    request.setAttribute("error", "Có lỗi cập nhật mật khẩu vào cơ sở dữ liệu! Vui lòng liên hệ quản trị viên.");
                } else {
                    request.setAttribute("error", "Không thể gửi email! Vui lòng kiểm tra lại địa chỉ email hoặc liên hệ quản trị viên.");
                }
            } else {
                request.setAttribute("error", "Sai Họ tên hoặc Email!");
            }
        } catch (Exception e) {
            request.setAttribute("error", "Có lỗi xảy ra trong quá trình khôi phục: " + e.getMessage());
        }
        request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
    }
    /**
     * 4. Phân luồng trang Quản lý sau Đăng nhập (Role Selection)
     */
    private void handleRoleSelection(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String role = request.getParameter("role");
        if (role == null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        switch (role.trim().toLowerCase()) {
            case "pos":
                response.sendRedirect(request.getContextPath() + "/sale/pos");
                break;
            case "management":
                response.sendRedirect(request.getContextPath() + "/management/dashboard");
                break;
            case "report":
                response.sendRedirect(request.getContextPath() + "/report/dashboard");
                break;
            case "system":
                response.sendRedirect(request.getContextPath() + "/system/config");
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/dashboard");
                break; 
                        
        }
    }
    private String getRedirectPath(Employee employee) {
        String redirectUrl = "/dashboard/owner"; // Default fallback
        if (employee != null && employee.getRoleName() != null) {
            String role = employee.getRoleName().trim().toLowerCase();
            switch (role) {
                case "admin":
                case "owner":
                case "storemanager":
                    redirectUrl = "/dashboard/owner";
                    break;
                case "warehousestaff":
                    redirectUrl = "/inventory/dashboard";
                    break;
                case "salesstaff":
                    redirectUrl = "/sales";
                    break;
            }
        }
        return redirectUrl;
    }
}
