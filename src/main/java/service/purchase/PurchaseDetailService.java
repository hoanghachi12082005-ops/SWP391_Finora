package service.purchase;

import dao.purchase.PurchaseDetailDAO;
import model.PurchaseDetail;
import java.util.List;

public class PurchaseDetailService {
    private final PurchaseDetailDAO dao = new PurchaseDetailDAO();

    public List<PurchaseDetail> findByOrderId(int orderId) { return dao.findByOrderId(orderId); }
}
