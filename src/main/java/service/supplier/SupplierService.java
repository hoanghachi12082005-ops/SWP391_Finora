package com.storemanagement.service.supplier;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.supplier.SupplierDAO;
import com.storemanagement.model.Supplier;

public class SupplierService extends GenericService<Supplier> {
    public SupplierService() {
        super(new SupplierDAO());
    }
}
