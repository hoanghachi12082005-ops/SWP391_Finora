package service.purchase;

import dao.purchase.PurchaseOrderDAO;
import model.PurchaseOrder;
import java.util.List;

public class PurchaseOrderService {
    private final PurchaseOrderDAO dao = new PurchaseOrderDAO();

    public List<PurchaseOrder> findAll() { return dao.findAll(); }
    public PurchaseOrder findById(int id) { return dao.findById(id); }
}
