package service.supplier;

import dao.supplier.SupplierDAO;
import model.Supplier;
import java.util.List;

public class SupplierService {
    private final SupplierDAO dao = new SupplierDAO();

    public List<Supplier> findAll() { return dao.findAll(); }
    public Supplier findById(int id) { return dao.findById(id); }
    public boolean insert(Supplier s) { return dao.insert(s); }
    public boolean update(Supplier s) { return dao.update(s); }
    public boolean softDelete(int id) { return dao.softDelete(id); }
}
