package service.sales;

import service.common.GenericService;

import dao.sales.OrderDAO;
import model.Order;

public class OrderService extends GenericService<Order> {
    public OrderService() {
        super(new OrderDAO());
    }
}
