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
        System.out.println("[DEBUG] Request Login nhận được username: [" + emailOrPhone + "]");
        try {
            // Xác thực thông tin qua tầng xử lý mật khẩu băm AuthService
            Employee employee = authService.login(emailOrPhone, password);
            HttpSession session = request.getSession(true);
            session.setAttribute("currentUser", employee);
            System.out.println("Session ID Login = " + session.getId());
            System.out.println("CurrentUser Login = " + session.getAttribute("currentUser"));
            response.sendRedirect(request.getContextPath() + getRedirectPath(employee));
        } catch (RuntimeException e) {
            System.err.println("[DEBUG] Đăng nhập thất bại do: " + e.getMessage());
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
                boolean updated = employeeDAO.updatePasswordByEmail(email.trim(), hashedPassword);
                if (updated) {
                    boolean sent = EmailUtil.sendPasswordEmail(email.trim(), fullName.trim(), newPassword);
                    if (sent) {
                        request.setAttribute("successMessage", "Mật khẩu mới đã được gửi thành công qua email của bạn!");
                        request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
                        return;
                    }
                    request.setAttribute("error", "Hệ thống đã cập nhật mật khẩu mới nhưng tiến trình gửi Email bị lỗi!");
                } else {
                    request.setAttribute("error", "Không thể cập nhật mật khẩu mới vào Cơ sở dữ liệu!");
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
                    redirectUrl = "/orders/create";
                    break;
            }
        }
        return redirectUrl;
    }
}
