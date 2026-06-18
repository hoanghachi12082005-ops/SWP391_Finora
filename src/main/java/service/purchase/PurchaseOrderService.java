package service.purchase;

import service.common.GenericService;

import dao.purchase.PurchaseOrderDAO;
import model.PurchaseOrder;

public class PurchaseOrderService extends GenericService<PurchaseOrder> {
    public PurchaseOrderService() {
        super(new PurchaseOrderDAO());
    }
}
