package service.supplier;

import dao.supplier.SupplierDAO;
import model.Supplier;

import java.util.List;

public class SupplierService {

    private final SupplierDAO dao = new SupplierDAO();

    public int countSuppliers(
            String keyword,
            String status) {

        return dao.countSuppliers(keyword, status);
    }

    public List<Supplier> getSuppliersPaging(
        String keyword,
        String status,
        int page,
        int pageSize){

        return dao.getSuppliersPaging(
                keyword,
                status,
                page,
                pageSize);
    }

    public Supplier getById(int id) {
        return dao.getById(id);
    }

    public boolean insert(Supplier s) {
        return dao.insert(s);
    }

    public boolean update(Supplier s) {
        return dao.update(s);
    }

    public boolean delete(int id) {
        return dao.delete(id);
    }
}
