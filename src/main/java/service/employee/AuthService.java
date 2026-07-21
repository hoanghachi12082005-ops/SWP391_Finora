package service.employee;

import dao.employee.EmployeeDAO;
import model.Employee;
import util.security.PasswordUtil;

public class AuthService {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    /**
     * Chức năng Đăng nhập (Login)
     */
    public Employee login(String username, String password, jakarta.servlet.http.HttpSession session) {

        Employee employee = employeeDAO.findByEmailOrPhone(username.trim());

        if (employee == null) {
            throw new RuntimeException("Email/số điện thoại không tồn tại");
        }

        // ─── DEBUG: Log thông tin password ─────────────────────────
        String dbHash = employee.getPasswordHash();
        System.out.println("[DEBUG AUTH] ===== BẮT ĐẦU SO SÁNH MẬT KHẨU =====");
        System.out.println("[DEBUG AUTH] Password người dùng nhập: [" + password + "]");
        System.out.println("[DEBUG AUTH] PasswordHash từ DB    : [" + dbHash + "]");
        System.out.println("[DEBUG AUTH] Độ dài hash DB        : " + (dbHash != null ? dbHash.length() : 0) + " ký tự");
        System.out.println("[DEBUG AUTH] Có phải BCrypt hash?  : " + PasswordUtil.isHashed(dbHash));
        System.out.println("[DEBUG AUTH] Hash thực tế của password nhập: [" + PasswordUtil.hash(password) + "]");
        // ─────────────────────────────────────────────────────────────

        if (!"ACTIVE".equalsIgnoreCase(employee.getStatus())) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa hoặc chưa được kích hoạt. Vui lòng liên hệ Admin/Owner để mở khóa.");
        }

        boolean verify = PasswordUtil.verify(password, dbHash);

        // ─── DEBUG: Kết quả verify ──────────────────────────────────
        System.out.println("[DEBUG AUTH] Kết quả verify(password, dbHash): " + verify);
        System.out.println("[DEBUG AUTH] ===== KẾT THÚC SO SÁNH MẬT KHẨU =====\n");
        // ─────────────────────────────────────────────────────────────

        if (!verify) {
            employeeDAO.incrementFailedLoginCount(employee.getEmployeeID());
            int failedAttempts = employee.getCountLoginFail() + 1;
            int remaining = Employee.MAX_FAILED_LOGIN - failedAttempts;
            if (remaining <= 0) {
                employeeDAO.lockEmployee(employee.getEmployeeID());
                throw new RuntimeException("Tài khoản của bạn đã bị khóa do đăng nhập sai quá 5 lần. Vui lòng liên hệ Admin/Owner để mở khóa.");
            } else {
                throw new RuntimeException("Mật khẩu không chính xác. Bạn còn " + remaining + " lần nhập lại.");
            }
        }

        // Reset login failures on success
        employeeDAO.resetFailedLoginCount(employee.getEmployeeID());

        return employee;
    }

    /**
     * Chức năng Đăng ký tài khoản (Register)
     */
    public boolean register(String fullName,
            String email,
            String phone,
            String password,
            int roleId,
            int branchId) {

        // 1. Kiểm tra tính hợp lệ của dữ liệu (Validation)
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new RuntimeException("Họ tên không được để trống");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email không được để trống");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Mật khẩu không được để trống");
        }

        // 2. Kiểm tra trùng lặp email hệ thống
        if (employeeDAO.existsByEmail(email.trim(), null)) {
            throw new RuntimeException("Email này đã được sử dụng trong hệ thống");
        }

        // 3. Khởi tạo thực thể nhân viên mới
        Employee employee = new Employee();
        employee.setFullName(fullName.trim());
        employee.setEmail(email.trim());
        employee.setPhone(phone != null ? phone.trim() : null);

        // 4. Mã hóa mật khẩu đầu vào sang chuỗi BCrypt an toàn
        employee.setPasswordHash(PasswordUtil.hash(password));

        employee.setRoleID(roleId);
        // Nếu là Owner/Admin đăng ký tự do, branchId truyền vào có thể xử lý null
        employee.setBranchID(branchId > 0 ? branchId : null);
        employee.setStatus("active");

        // 5. Đồng bộ gọi hàm insertEmployee chính xác trong EmployeeDAO
        return employeeDAO.insert(employee);
    }
}
