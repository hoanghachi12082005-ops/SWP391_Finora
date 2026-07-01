package service.inventory;

import dao.inventory.InventoryItemDAO;
import model.InventoryItem;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class InventoryItemService {
    private final InventoryItemDAO dao = new InventoryItemDAO();

    public List<InventoryItem> findAll() { return dao.findAll(); }
    public InventoryItem findById(int id) { return dao.findById(id); }
    public List<InventoryItem> findByWarehouseId(int warehouseId) { return dao.findByWarehouseId(warehouseId); }
    public void deductStock(int productId, int warehouseId, int quantity, Connection conn) throws SQLException {
        dao.deductStock(productId, warehouseId, quantity, conn);
    }
}
