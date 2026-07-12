package service.inventory;

import dao.inventory.InventoryDAO;
import dao.inventory.StockTransferDAO;
import model.StockTransfer;
import model.StockTransferDetail;
import util.database.DBContext;
import java.sql.Connection;
import java.util.List;

public class TransferService {

    private final StockTransferDAO transferDAO = new StockTransferDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    /**
     * Owner duyệt toàn bộ phiếu tổng
     */
    public void approveMasterTransfer(String transferCode, int approverId) throws Exception {
        // Owner duyệt phiếu tổng -> chuyển tất cả phiếu con sang PENDING_PARTNER (Chờ đối tác duyệt)
        transferDAO.updateStatusByCode(transferCode, "PENDING_PARTNER", approverId);
    }

    /**
     * Owner từ chối toàn bộ phiếu tổng
     */
    public void rejectMasterTransfer(String transferCode, int rejecterId) throws Exception {
        // Owner từ chối -> CANCELLED
        transferDAO.updateStatusByCode(transferCode, "CANCELLED", rejecterId);
    }

    /**
     * Đối tác (Owner/chủ cửa hàng) duyệt/từ chối toàn bộ các phiếu con liên quan tới kho của họ
     */
    public void partnerApprove(String transferCode, int partnerWarehouseId) throws Exception {
        // Kho đối tác duyệt -> Chuyển sang APPROVED_DISPATCH (chờ xuất kho)
        transferDAO.updateStatusForPartner(transferCode, partnerWarehouseId, "APPROVED_DISPATCH");
    }

    public void partnerReject(String transferCode, int partnerWarehouseId) throws Exception {
        // Kho đối tác từ chối -> Chuyển sang PARTNER_REJECTED
        transferDAO.updateStatusForPartner(transferCode, partnerWarehouseId, "PARTNER_REJECTED");
    }
    
    public void partnerApproveAll(String transferCode) throws Exception {
        transferDAO.updateStatusByCode(transferCode, "APPROVED_DISPATCH", null);
    }
    
    public void partnerRejectAll(String transferCode) throws Exception {
        transferDAO.updateStatusByCode(transferCode, "PARTNER_REJECTED", null);
    }

    /**
     * Thủ kho xác nhận xuất hàng (ở kho gửi)
     */
    public void dispatchTransfer(int transferId, int empId) throws Exception {
        StockTransfer transfer = transferDAO.findById(transferId);
        if (transfer == null || !"APPROVED_DISPATCH".equals(transfer.getStatus())) {
            throw new Exception("Phiếu điều chuyển không hợp lệ hoặc chưa được duyệt.");
        }

        List<StockTransferDetail> details = transferDAO.getTransferDetails(transferId);

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transferDAO.updateStatus(conn, transferId, "IN_TRANSIT");

                for (StockTransferDetail d : details) {
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

    /**
     * Thủ kho từ chối xuất hàng (ở kho gửi)
     */
    public void rejectDispatch(int transferId, int empId) throws Exception {
        transferDAO.updateStatus(transferId, "DISPATCH_REJECTED", empId);
    }

    /**
     * Thủ kho xác nhận nhập hàng (ở kho nhận)
     */
    public void receiveTransfer(int transferId, int empId) throws Exception {
        StockTransfer transfer = transferDAO.findById(transferId);
        if (transfer == null || !"IN_TRANSIT".equals(transfer.getStatus())) {
            throw new Exception("Phiếu điều chuyển không hợp lệ hoặc chưa được xuất kho.");
        }

        List<StockTransferDetail> details = transferDAO.getTransferDetails(transferId);

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transferDAO.updateStatus(conn, transferId, "COMPLETED", empId);

                for (StockTransferDetail d : details) {
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

    /**
     * Thủ kho từ chối nhập hàng (ở kho nhận)
     */
    public void rejectReceive(int transferId, int empId) throws Exception {
        transferDAO.updateStatus(transferId, "RECEIVE_REJECTED", empId);
    }

    /**
     * Hủy phần yêu cầu điều chuyển kho (ở kho gửi)
     */
    public void cancelTransfer(int transferId, int empId) throws Exception {
        transferDAO.updateStatus(transferId, "CANCELLED", empId);
    }
}
