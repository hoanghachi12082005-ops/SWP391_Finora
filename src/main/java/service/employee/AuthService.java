package service.employee;

import dao.employee.EmployeeDAO;
import model.Employee;
import util.security.PasswordUtil;

public class AuthService {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    /**
     * Chức năng Đăng nhập (Login)
     */
    public Employee login(String username, String password) {

        System.out.println("Username nhập: " + username);

        Employee employee = employeeDAO.findByEmailOrPhone(username.trim());

        System.out.println("Employee: " + employee);

        if (employee == null) {
            throw new RuntimeException("Email/số điện thoại không tồn tại");
        }

        System.out.println("Status DB: " + employee.getStatus());

        if (!"ACTIVE".equalsIgnoreCase(employee.getStatus())) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa hoặc chưa được kích hoạt");
        }

        System.out.println("Hash DB: " + employee.getPasswordHash());

        boolean verify = PasswordUtil.verify(password, employee.getPasswordHash());

        System.out.println("Verify result: " + verify);

        if (!verify) {
            throw new RuntimeException("Mật khẩu không chính xác");
        }

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
        if (employeeDAO.existsByEmail(email.trim(), phone.trim(),null)) {
            throw new RuntimeException("Email này đã được sử dụng trong hệ thống");
        }

        // 3. Khởi tạo thực thể nhân viên mới
        Employee employee = new Employee();
        employee.setFullName(fullName.trim());
        employee.setEmail(email.trim());
        employee.setPhone(phone != null ? phone.trim() : null);

        // 4. Mã hóa mật khẩu đầu vào sang chuỗi BCrypt an toàn
        employee.setPasswordHash(PasswordUtil.hash(password));

        employee.setRoleId(roleId);
        // Nếu là Owner/Admin đăng ký tự do, branchId truyền vào có thể xử lý null
        employee.setBranchId(branchId > 0 ? branchId : null);
        employee.setStatus("active");
        
        

        // 5. Đồng bộ gọi hàm insertEmployee chính xác trong EmployeeDAO
        return employeeDAO.insert(employee);
    }
}
