package service.supplier;

import service.common.GenericService;

import dao.supplier.SupplierDAO;
import model.Supplier;

public class SupplierService extends GenericService<Supplier> {
    public SupplierService() {
        super(new SupplierDAO());
    }
}
