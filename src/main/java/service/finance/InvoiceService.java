package com.storemanagement.service.finance;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.finance.InvoiceDAO;
import com.storemanagement.model.Invoice;

public class InvoiceService extends GenericService<Invoice> {
    public InvoiceService() {
        super(new InvoiceDAO());
    }
}
