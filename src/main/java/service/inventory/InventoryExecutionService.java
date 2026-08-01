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

                // Ghi nhận phiếu chi vào Sổ quỹ (bảng payment) nếu đây là đơn nhập hàng
                if ("PURCHASE".equals(order.getOrderType())) {
                    try {
                        dao.finance.PaymentDAO paymentDAO = new dao.finance.PaymentDAO();
                        model.Payment payment = new model.Payment();
                        payment.setOrderId(orderId);
                        payment.setAmount(order.getTotalAmount());
                        payment.setStatus("PAID");
                        payment.setName(order.getOrderCode());
                        payment.setPaymentType("EXPENSE");
                        payment.setMethod(order.getPaymentMethod() != null ? order.getPaymentMethod() : "BANK_TRANSFER");
                        payment.setDescription("Chi tiền nhập hàng cho đơn " + order.getOrderCode());
                        payment.setEmployeeId(approverId > 0 ? approverId : order.getEmpId());
                        payment.setBranchId(order.getBranchId() > 0 ? order.getBranchId() : 1);
                        paymentDAO.insert(conn, payment);
                    } catch (Exception payEx) {
                        System.err.println("WARN: Lỗi tạo phiếu chi thanh toán Sổ quỹ cho đơn nhập #" + orderId + ": " + payEx.getMessage());
                        payEx.printStackTrace();
                    }
                }
                
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void confirmReceivePurchaseOrder(int orderId, java.util.Map<Integer, Integer> actualQuantities, int receiverId) throws Exception {
        confirmReceivePurchaseOrder(orderId, null, actualQuantities, receiverId);
    }

    public void confirmReceivePurchaseOrder(int orderId, Integer targetSupplierId, java.util.Map<Integer, Integer> actualQuantities, int receiverId) throws Exception {
        Order order = orderDAO.findById(orderId);
        if (order == null) {
            throw new Exception("Đơn nhập hàng không tồn tại.");
        }
        if (!"PURCHASE".equalsIgnoreCase(order.getOrderType())) {
            throw new Exception("Đơn hàng này không phải là đơn nhập hàng từ nhà cung cấp.");
        }

        List<OrderDetail> details = orderDAO.findDetailsByOrderId(orderId);
        double supplierTotal = 0.0;
        String supplierNameForNote = "";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (OrderDetail d : details) {
                    // If targetSupplierId is specified, skip items from other suppliers
                    if (targetSupplierId != null && targetSupplierId > 0) {
                        if (d.getSupplierId() == null || d.getSupplierId().intValue() != targetSupplierId.intValue()) {
                            continue;
                        }
                    }

                    // Skip items already completed
                    if ("COMPLETED".equalsIgnoreCase(d.getSupplierStatus())) {
                        continue;
                    }

                    int actualQty = d.getQuantity();
                    if (actualQuantities != null && actualQuantities.containsKey(d.getOrderDetailId())) {
                        actualQty = actualQuantities.get(d.getOrderDetailId());
                    }
                    if (actualQty < 0) actualQty = 0;

                    // Update detail in DB as COMPLETED for this supplier
                    orderDAO.updateOrderDetailSupplierStatus(conn, d.getOrderDetailId(), actualQty, d.getUnitPrice(), "COMPLETED");
                    double itemTotal = actualQty * d.getUnitPrice();
                    supplierTotal += itemTotal;

                    if (d.getSupplierName() != null && !d.getSupplierName().isEmpty()) {
                        supplierNameForNote = d.getSupplierName();
                    }

                    if (actualQty > 0) {
                        int beforeQty = inventoryDAO.getStockInTransaction(conn, d.getProductId(), order.getWarehouseId());
                        inventoryDAO.increaseStock(conn, order.getWarehouseId(), d.getProductId(), actualQty);
                        
                        inventoryDAO.logCustomStockTransaction(conn, order.getWarehouseId(), d.getProductId(),
                                "PURCHASE_ORDER", orderId, "IMPORT",
                                actualQty, beforeQty, beforeQty + actualQty,
                                "Nhập hàng thực tế (" + (supplierNameForNote.isEmpty() ? "NCC" : supplierNameForNote) + ") phiếu " + order.getOrderCode(), receiverId);
                    }
                }

                // Check if all order details for this order are now COMPLETED
                List<OrderDetail> updatedDetails = orderDAO.findDetailsByOrderId(orderId);
                boolean allCompleted = true;
                double grandTotalActual = 0.0;
                for (OrderDetail ud : updatedDetails) {
                    int act = ud.getActualQuantity() != null ? ud.getActualQuantity() : ud.getQuantity();
                    grandTotalActual += act * ud.getUnitPrice();
                    if (!"COMPLETED".equalsIgnoreCase(ud.getSupplierStatus())) {
                        allCompleted = false;
                    }
                }

                // Update Order totals and status
                orderDAO.updateOrderTotals(conn, orderId, grandTotalActual, grandTotalActual);
                if (allCompleted) {
                    orderDAO.updateStatus(conn, orderId, "COMPLETED", receiverId);
                } else {
                    orderDAO.updateStatus(conn, orderId, "IN_TRANSIT", receiverId);
                }

                // Payment expense voucher for this received batch/supplier
                if (supplierTotal > 0) {
                    try {
                        dao.finance.PaymentDAO paymentDAO = new dao.finance.PaymentDAO();
                        model.Payment payment = new model.Payment();
                        payment.setOrderId(orderId);
                        payment.setAmount(supplierTotal);
                        payment.setStatus("PAID");
                        payment.setName(order.getOrderCode());
                        payment.setPaymentType("EXPENSE");
                        payment.setMethod(order.getPaymentMethod() != null ? order.getPaymentMethod() : "BANK_TRANSFER");
                        String note = "Chi tiền nhập hàng thực tế" + (supplierNameForNote.isEmpty() ? "" : " (NCC " + supplierNameForNote + ")") + " cho đơn " + order.getOrderCode();
                        payment.setDescription(note);
                        payment.setEmployeeId(receiverId > 0 ? receiverId : order.getEmpId());
                        payment.setBranchId(order.getBranchId() > 0 ? order.getBranchId() : 1);
                        paymentDAO.insert(conn, payment);
                    } catch (Exception payEx) {
                        System.err.println("WARN: Lỗi tạo phiếu chi thanh toán Sổ quỹ cho đơn nhập #" + orderId + ": " + payEx.getMessage());
                        payEx.printStackTrace();
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
