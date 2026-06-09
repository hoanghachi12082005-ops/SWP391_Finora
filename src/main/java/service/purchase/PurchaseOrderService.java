package com.storemanagement.service.purchase;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.purchase.PurchaseOrderDAO;
import com.storemanagement.model.PurchaseOrder;

public class PurchaseOrderService extends GenericService<PurchaseOrder> {
    public PurchaseOrderService() {
        super(new PurchaseOrderDAO());
    }
}
