package service.sales;

import dao.sales.OrderDetailDAO;
import model.OrderDetail;
import java.util.List;

public class OrderDetailService {
    private final OrderDetailDAO dao = new OrderDetailDAO();

    public List<OrderDetail> findByOrderId(int orderId) { return dao.findByOrderId(orderId); }
}
