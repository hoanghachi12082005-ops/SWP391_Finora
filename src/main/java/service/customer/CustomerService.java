package service.customer;

import dao.customer.CustomerDAO;
import model.Customer;
import java.util.regex.Pattern;

public class CustomerService {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0[0-9]{9,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    public Customer getCustomerWithPoints(int customerId) {
        return customerDAO.findById(customerId);
    }

    public boolean updateCustomerFromPOS(Customer customer) {
        return customerDAO.update(customer, false, 0, 0);
    }

    public boolean isEmailOrPhoneExists(String email, String phone, Integer excludeId) {
        return customerDAO.isEmailOrPhoneExists(email, phone, excludeId);
    }

    public String validatePhone(String phone) {
        if (phone == null || phone.isBlank()) return "Số điện thoại không được để trống.";
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) return "Số điện thoại không hợp lệ (phải bắt đầu bằng 0 và 10-11 số).";
        return null;
    }

    public String validateEmail(String email) {
        if (email != null && !email.isBlank() && !EMAIL_PATTERN.matcher(email.trim()).matches())
            return "Email không hợp lệ.";
        return null;
    }

    public String validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) return fieldName + " không được để trống.";
        return null;
    }
}
