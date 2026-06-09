package com.storemanagement.service.sales;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.sales.OrderDetailDAO;
import com.storemanagement.model.OrderDetail;

public class OrderDetailService extends GenericService<OrderDetail> {
    public OrderDetailService() {
        super(new OrderDetailDAO());
    }
}
