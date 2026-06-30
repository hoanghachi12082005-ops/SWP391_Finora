package service.inventory;

import dao.inventory.StockTransactionDAO;
import model.StockTransaction;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class StockTransactionService {
    private final StockTransactionDAO dao = new StockTransactionDAO();

    public List<StockTransaction> findByWarehouseId(int warehouseId) { return dao.findByWarehouseId(warehouseId); }
    public void insert(int warehouseId, int productId, String refType, Integer refId, String txType, int quantity, int beforeQty, int afterQty, Integer createdBy, Connection conn) throws SQLException {
        dao.insert(warehouseId, productId, refType, refId, txType, quantity, beforeQty, afterQty, createdBy, conn);
    }
}
