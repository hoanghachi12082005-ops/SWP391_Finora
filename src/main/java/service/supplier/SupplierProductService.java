package service.supplier;

import dao.supplier.SupplierProductDAO;
import model.Product;

import java.util.List;
import java.util.Map;

public class SupplierProductService {
    private final SupplierProductDAO dao = new SupplierProductDAO();

    public Map<Integer, Map<Integer, Double>> getSupplierProductMap() {
        return dao.getSupplierProductMap();
    }

    public List<Integer> getLinkedProductIds(int supplierId) {
        return dao.getLinkedProductIds(supplierId);
    }

    public Map<Integer, Double> getLinkedProductsWithPrices(int supplierId) {
        return dao.getLinkedProductsWithPrices(supplierId);
    }

    public boolean saveAssociations(int supplierId, List<Integer> productIds, List<Double> prices) {
        return dao.saveAssociations(supplierId, productIds, prices);
    }
}
