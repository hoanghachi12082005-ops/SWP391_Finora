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

}
