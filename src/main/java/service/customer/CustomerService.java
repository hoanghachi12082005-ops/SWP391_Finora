package com.storemanagement.service.customer;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.customer.CustomerDAO;
import com.storemanagement.model.Customer;

public class CustomerService extends GenericService<Customer> {
    public CustomerService() {
        super(new CustomerDAO());
    }
}
