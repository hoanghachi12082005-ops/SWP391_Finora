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

    public int countActiveSuppliers() {
        return dao.countActiveSuppliers();
    }

    public int countInactiveSuppliers() {
        return dao.countInactiveSuppliers();
    }

    public Supplier getById(int id) {
        return dao.getById(id);
    }

    public boolean existsByNameOrPhone(String name, String phone) {
        return dao.existsByNameOrPhone(name, phone);
    }

    public boolean save(Supplier s) {
        return dao.save(s);
    }

    public boolean delete(int id) {
        return dao.delete(id);
    }
}
