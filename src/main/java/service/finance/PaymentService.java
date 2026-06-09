package com.storemanagement.service.finance;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.finance.PaymentDAO;
import com.storemanagement.model.Payment;

public class PaymentService extends GenericService<Payment> {
    public PaymentService() {
        super(new PaymentDAO());
    }
}
