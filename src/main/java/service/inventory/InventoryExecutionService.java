package service.inventory;

import dao.sales.OrderDAO;
import dao.inventory.InventoryDAO;
import dao.inventory.StockTransactionDAO;
import dao.inventory.StockTransferDAO;
import dao.inventory.InventoryCheckDAO;
import model.Order;
import model.OrderDetail;
import model.StockTransaction;
import model.StockTransfer;
import model.InventoryCheck;
import model.InventoryCheckDetail;
import util.database.DBContext;
import java.sql.Connection;
import java.util.List;

public class InventoryExecutionService {

    private OrderDAO orderDAO = new OrderDAO();
    private InventoryDAO inventoryDAO = new InventoryDAO();
    private StockTransactionDAO txDAO = new StockTransactionDAO();
    private StockTransferDAO transferDAO = new StockTransferDAO();

    public void executeOrder(int orderId, int approverId) throws Exception {
        Order order = orderDAO.findById(orderId);
        if (order == null || order.getStatus() != Order.OrderStatus.PENDING) {
            throw new Exception("Đơn hàng không tồn tại hoặc đã được xử lý.");
        }

        List<OrderDetail> details = orderDAO.getOrderDetailById(orderId);
        
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update Order Status
                orderDAO.updateStatus(conn, orderId, "COMPLETED", approverId);
                
                // Update Stock and Insert Transactions
                for (OrderDetail d : details) {
                    int beforeQty = inventoryDAO.getStockInTransaction(conn, d.getProductId(), order.getWarehouseId());
                    
                    if ("PURCHASE".equals(order.getOrderType())) {
                        inventoryDAO.increaseStock(conn, order.getWarehouseId(), d.getProductId(), d.getQuantity());
                        
                        inventoryDAO.logCustomStockTransaction(conn, order.getWarehouseId(), d.getProductId(),
                                "PURCHASE_ORDER", orderId, "IMPORT",
                                d.getQuantity(), beforeQty, beforeQty + d.getQuantity(), 
                                "Nhập hàng từ phiếu " + order.getOrderCode(), approverId);
                    } else if ("EXPORT".equals(order.getOrderType())) {
                        inventoryDAO.deductStock(conn, d.getProductId(), order.getWarehouseId(), d.getQuantity());
                        
                        inventoryDAO.logCustomStockTransaction(conn, order.getWarehouseId(), d.getProductId(),
                                "EXPORT_ORDER", orderId, "EXPORT",
                                d.getQuantity(), beforeQty, beforeQty - d.getQuantity(), 
                                "Xuất hàng từ phiếu " + order.getOrderCode(), approverId);
                    }
                }
                
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void dispatchTransfer(int transferId, int empId) throws Exception {
        StockTransfer transfer = transferDAO.findById(transferId);
        if (transfer == null || !"APPROVED_DISPATCH".equals(transfer.getStatus())) {
            throw new Exception("Phiếu điều chuyển không hợp lệ hoặc chưa được duyệt.");
        }

        List<model.StockTransferDetail> details = transferDAO.getTransferDetails(transferId);

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transferDAO.updateStatus(conn, transferId, "IN_TRANSIT");

                for (model.StockTransferDetail d : details) {
                    int beforeQty = inventoryDAO.getStockInTransaction(conn, d.getProductId(), transfer.getFromWarehouseId());
                    inventoryDAO.deductStock(conn, d.getProductId(), transfer.getFromWarehouseId(), d.getQuantity());

                    inventoryDAO.logCustomStockTransaction(conn, transfer.getFromWarehouseId(), d.getProductId(),
                            "STOCK_TRANSFER", transferId, "TRANSFER_OUT",
                            d.getQuantity(), beforeQty, beforeQty - d.getQuantity(),
                            "Xuất điều chuyển kho " + transfer.getTransferCode(), empId);
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void receiveTransfer(int transferId, int empId) throws Exception {
        StockTransfer transfer = transferDAO.findById(transferId);
        if (transfer == null || !"IN_TRANSIT".equals(transfer.getStatus())) {
            throw new Exception("Phiếu điều chuyển không hợp lệ hoặc chưa xuất kho.");
        }

        List<model.StockTransferDetail> details = transferDAO.getTransferDetails(transferId);

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transferDAO.updateStatus(conn, transferId, "COMPLETED", empId);

                for (model.StockTransferDetail d : details) {
                    int beforeQty = inventoryDAO.getStockInTransaction(conn, d.getProductId(), transfer.getToWarehouseId());
                    inventoryDAO.increaseStock(conn, transfer.getToWarehouseId(), d.getProductId(), d.getQuantity());

                    inventoryDAO.logCustomStockTransaction(conn, transfer.getToWarehouseId(), d.getProductId(),
                            "STOCK_TRANSFER", transferId, "TRANSFER_IN",
                            d.getQuantity(), beforeQty, beforeQty + d.getQuantity(),
                            "Nhập điều chuyển kho " + transfer.getTransferCode(), empId);
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void approveInventoryCheck(int checkId, int approverId) throws Exception {
        InventoryCheckDAO checkDAO = new InventoryCheckDAO();
        InventoryCheck check = checkDAO.findById(checkId);
        if (check == null || !"PENDING".equals(check.getStatus())) {
            throw new Exception("Phiếu kiểm kho không tồn tại hoặc đã được xử lý.");
        }

        List<InventoryCheckDetail> details = checkDAO.getCheckDetails(checkId);

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                checkDAO.updateStatus(conn, checkId, "APPROVED", approverId);

                for (InventoryCheckDetail d : details) {
                    int beforeQty = inventoryDAO.getStockInTransaction(conn, d.getProductId(), check.getWarehouseId());
                    int afterQty = d.getActualQty();
                    int diff = d.getDiscrepancy(); // actual - system

                    inventoryDAO.updateStockQty(conn, check.getWarehouseId(), d.getProductId(), afterQty);

                    String txType = diff >= 0 ? "CHECK_IN" : "CHECK_OUT";
                    String noteStr = "Kiểm kê kho " + check.getCheckCode() + ": " + (d.getNote() != null ? d.getNote() : "");
                    inventoryDAO.logCustomStockTransaction(conn, check.getWarehouseId(), d.getProductId(),
                            "INVENTORY_CHECK", checkId, txType,
                            Math.abs(diff), beforeQty, afterQty,
                            noteStr, approverId);
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void executeStockBalance(int checkId, int executorId) throws Exception {
        InventoryCheckDAO checkDAO = new InventoryCheckDAO();
        InventoryCheck check = checkDAO.findById(checkId);
        if (check == null) {
            throw new Exception("Phiếu kiểm kho không tồn tại.");
        }

        List<InventoryCheckDetail> details = checkDAO.getCheckDetails(checkId);

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                checkDAO.updateStatus(conn, checkId, "APPROVED", executorId);

                for (InventoryCheckDetail d : details) {
                    int beforeQty = inventoryDAO.getStockInTransaction(conn, d.getProductId(), check.getWarehouseId());
                    int afterQty = d.getActualQty();
                    int diff = d.getDiscrepancy(); // actual - system

                    inventoryDAO.updateStockQty(conn, check.getWarehouseId(), d.getProductId(), afterQty);

                    String txType = diff >= 0 ? "CHECK_IN" : "CHECK_OUT";
                    String noteStr = "Kiểm kê kho " + check.getCheckCode() + ": " + (d.getNote() != null ? d.getNote() : "");
                    inventoryDAO.logCustomStockTransaction(conn, check.getWarehouseId(), d.getProductId(),
                            "INVENTORY_CHECK", checkId, txType,
                            Math.abs(diff), beforeQty, afterQty,
                            noteStr, executorId);
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
