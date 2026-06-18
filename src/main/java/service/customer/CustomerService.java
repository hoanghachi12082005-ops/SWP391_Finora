package service.customer;

import service.common.GenericService;

import dao.customer.CustomerDAO;
import model.Customer;

public class CustomerService extends GenericService<Customer> {
    public CustomerService() {
        super(new CustomerDAO());
    }
}
