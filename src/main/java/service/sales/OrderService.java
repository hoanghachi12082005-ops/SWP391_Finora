package com.storemanagement.service.sales;

import com.storemanagement.service.common.GenericService;

import com.storemanagement.dao.sales.OrderDAO;
import com.storemanagement.model.Order;

public class OrderService extends GenericService<Order> {
    public OrderService() {
        super(new OrderDAO());
    }
}
