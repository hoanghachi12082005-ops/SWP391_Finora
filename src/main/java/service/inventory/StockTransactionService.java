package service.inventory;

import dao.inventory.StockTransactionDAO;
import model.StockTransaction;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class StockTransactionService {
    private final StockTransactionDAO dao = new StockTransactionDAO();

    public List<StockTransaction> findByReference(String referenceType, int referenceId) throws SQLException {
        return dao.findByReference(referenceType, referenceId);
    }
    public void insert(int warehouseId, int productId, String refType, Integer refId, String txType, int quantity, int beforeQty, int afterQty, Integer createdBy, Connection conn) throws SQLException {
        StockTransaction tx = new StockTransaction();
        tx.setWarehouseId(warehouseId);
        tx.setProductId(productId);
        tx.setReferenceType(refType);
        tx.setReferenceId(refId);
        tx.setTransactionType(txType);
        tx.setQuantity(quantity);
        tx.setBeforeQuantity(beforeQty);
        tx.setAfterQuantity(afterQty);
        tx.setCreatedBy(createdBy);
        dao.insert(tx);
    }
}
