package com.storemanagement.service.purchase;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.purchase.PurchaseDetailDAO;
import com.storemanagement.model.PurchaseDetail;

public class PurchaseDetailService extends GenericService<PurchaseDetail> {
    public PurchaseDetailService() {
        super(new PurchaseDetailDAO());
    }
}
