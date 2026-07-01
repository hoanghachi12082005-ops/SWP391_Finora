package dao.inventory;

import model.InventoryTicket;
import model.InventoryTicketDetail;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryTicketDAO {

    public List<InventoryTicket> findAllByType(String ticketType, Integer warehouseId) throws SQLException {
        List<InventoryTicket> tickets = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT t.*, fw.warehouse_name as from_warehouse_name, " +
            "tw.warehouse_name as to_warehouse_name, e.fullName as created_by_name " +
            "FROM inventory_ticket t " +
            "LEFT JOIN warehouse fw ON t.from_warehouse_id = fw.warehouse_id " +
            "LEFT JOIN warehouse tw ON t.to_warehouse_id = tw.warehouse_id " +
            "LEFT JOIN Employee e ON t.created_by = e.emp_id " +
            "WHERE t.ticket_type = ?");
        if (warehouseId != null && warehouseId > 0) {
            sql.append(" AND (t.from_warehouse_id = ? OR t.to_warehouse_id = ?)");
        }
        sql.append(" ORDER BY t.created_at DESC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            stmt.setString(idx++, ticketType);
            if (warehouseId != null && warehouseId > 0) {
                stmt.setInt(idx++, warehouseId);
                stmt.setInt(idx, warehouseId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(extractTicket(rs));
                }
            }
        }
        return tickets;
    }

    public List<InventoryTicket> findAllByTypeAndStatus(String ticketType, Integer warehouseId, String status) throws SQLException {
        List<InventoryTicket> tickets = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT t.*, fw.warehouse_name as from_warehouse_name, " +
            "tw.warehouse_name as to_warehouse_name, e.fullName as created_by_name " +
            "FROM inventory_ticket t " +
            "LEFT JOIN warehouse fw ON t.from_warehouse_id = fw.warehouse_id " +
            "LEFT JOIN warehouse tw ON t.to_warehouse_id = tw.warehouse_id " +
            "LEFT JOIN Employee e ON t.created_by = e.emp_id " +
            "WHERE t.ticket_type = ?");
        
        if (status != null && !status.isEmpty()) {
            if ("COMPLETED_REJECTED".equals(status)) {
                sql.append(" AND t.status IN ('COMPLETED', 'REJECTED', 'COMPLETED_WITH_ERROR', 'CANCELLED')");
            } else if ("PENDING_IN_TRANSIT".equals(status)) {
                if ("TRANSFER_CHECK".equals(ticketType)) {
                    sql.append(" AND EXISTS (SELECT 1 FROM inventory_ticket parent WHERE parent.ticket_id = TRY_CAST(SUBSTRING(t.ticket_code, 4, 20) AS INT) AND parent.status NOT IN ('COMPLETED', 'REJECTED', 'COMPLETED_WITH_ERROR', 'CANCELLED'))");
                } else {
                    sql.append(" AND t.status IN ('PENDING', 'IN_TRANSIT')");
                }
            } else {
                sql.append(" AND t.status = ?");
            }
        }
        
        if (warehouseId != null && warehouseId > 0) {
            sql.append(" AND (t.from_warehouse_id = ? OR t.to_warehouse_id = ?)");
        }
        sql.append(" ORDER BY t.created_at DESC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            stmt.setString(idx++, ticketType);
            if (status != null && !status.isEmpty() && !"COMPLETED_REJECTED".equals(status) && !"PENDING_IN_TRANSIT".equals(status)) {
                stmt.setString(idx++, status);
            }
            if (warehouseId != null && warehouseId > 0) {
                stmt.setInt(idx++, warehouseId);
                stmt.setInt(idx, warehouseId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(extractTicket(rs));
                }
            }
        }
        return tickets;
    }

    public int getPendingCount(String ticketType, Integer warehouseId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM inventory_ticket WHERE status = 'PENDING'");
        if (ticketType != null) {
            sql.append(" AND ticket_type = ?");
        }
        if (warehouseId != null && warehouseId > 0) {
            sql.append(" AND (from_warehouse_id = ? OR to_warehouse_id = ?)");
        }
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (ticketType != null) stmt.setString(idx++, ticketType);
            if (warehouseId != null && warehouseId > 0) {
                stmt.setInt(idx++, warehouseId);
                stmt.setInt(idx, warehouseId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private InventoryTicket extractTicket(ResultSet rs) throws SQLException {
        InventoryTicket t = new InventoryTicket();
        t.setTicketId(rs.getInt("ticket_id"));
        t.setTicketCode(rs.getString("ticket_code"));
        t.setTicketType(rs.getString("ticket_type"));
        t.setFromWarehouseId(rs.getObject("from_warehouse_id") != null ? rs.getInt("from_warehouse_id") : null);
        t.setToWarehouseId(rs.getObject("to_warehouse_id") != null ? rs.getInt("to_warehouse_id") : null);
        t.setStatus(rs.getString("status"));
        t.setNote(rs.getString("note"));
        t.setCreatedBy(rs.getInt("created_by"));
        if (rs.getTimestamp("created_at") != null) {
            t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        t.setFromWarehouseName(rs.getString("from_warehouse_name"));
        t.setToWarehouseName(rs.getString("to_warehouse_name"));
        t.setCreatedByName(rs.getString("created_by_name"));
        t.setExportedBySender(rs.getBoolean("is_exported_by_sender"));
        t.setImportedByReceiver(rs.getBoolean("is_imported_by_receiver"));
        return t;
    }

    public void createExchangeTicket(InventoryTicket ticket, List<InventoryTicketDetail> details) throws SQLException {
        String insertTicketSql = "INSERT INTO inventory_ticket (ticket_code, ticket_type, from_warehouse_id, to_warehouse_id, status, created_by) VALUES (?, ?, ?, ?, ?, ?)";
        String insertDetailSql = "INSERT INTO inventory_ticket_detail (ticket_id, product_id, quantity, action_type) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtTicket = conn.prepareStatement(insertTicketSql, Statement.RETURN_GENERATED_KEYS)) {
                stmtTicket.setString(1, ticket.getTicketCode());
                stmtTicket.setString(2, ticket.getTicketType());
                if (ticket.getFromWarehouseId() != null) {
                    stmtTicket.setInt(3, ticket.getFromWarehouseId());
                } else {
                    stmtTicket.setNull(3, Types.INTEGER);
                }
                if (ticket.getToWarehouseId() != null) {
                    stmtTicket.setInt(4, ticket.getToWarehouseId());
                } else {
                    stmtTicket.setNull(4, Types.INTEGER);
                }
                stmtTicket.setString(5, ticket.getStatus());
                stmtTicket.setInt(6, ticket.getCreatedBy());
                
                stmtTicket.executeUpdate();
                
                try (ResultSet generatedKeys = stmtTicket.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int ticketId = generatedKeys.getInt(1);
                        
                        try (PreparedStatement stmtDetail = conn.prepareStatement(insertDetailSql)) {
                            for (InventoryTicketDetail detail : details) {
                                stmtDetail.setInt(1, ticketId);
                                stmtDetail.setInt(2, detail.getProductId());
                                stmtDetail.setInt(3, detail.getQuantity());
                                stmtDetail.setString(4, detail.getActionType());
                                stmtDetail.addBatch();
                            }
                            stmtDetail.executeBatch();
                        }
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            conn.commit();
        }
    }

    public InventoryTicket findById(int ticketId) throws SQLException {
        String sql = "SELECT t.*, " +
                     "fw.warehouse_name as from_warehouse_name, " +
                     "tw.warehouse_name as to_warehouse_name, " +
                     "e.fullName as created_by_name " +
                     "FROM inventory_ticket t " +
                     "LEFT JOIN warehouse fw ON t.from_warehouse_id = fw.warehouse_id " +
                     "LEFT JOIN warehouse tw ON t.to_warehouse_id = tw.warehouse_id " +
                      "LEFT JOIN Employee e ON t.created_by = e.emp_id " +
                      "WHERE t.ticket_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ticketId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractTicket(rs);
                }
            }
        }
        return null;
    }

    public InventoryTicket getTicketById(int ticketId) throws SQLException {
        String sql = "SELECT * FROM inventory_ticket WHERE ticket_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ticketId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    InventoryTicket t = new InventoryTicket();
                    t.setTicketId(rs.getInt("ticket_id"));
                    t.setTicketCode(rs.getString("ticket_code"));
                    t.setTicketType(rs.getString("ticket_type"));
                    t.setFromWarehouseId(rs.getInt("from_warehouse_id"));
                    t.setToWarehouseId(rs.getInt("to_warehouse_id"));
                    t.setStatus(rs.getString("status"));
                    t.setNote(rs.getString("note"));
                    t.setCreatedBy(rs.getInt("created_by"));
                    t.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
                    return t;
                }
            }
        }
        return null;
    }
    
    public List<InventoryTicketDetail> getTicketDetails(int ticketId) throws SQLException {
        List<InventoryTicketDetail> details = new ArrayList<>();
        String sql = "SELECT d.*, p.product_name FROM inventory_ticket_detail d JOIN product p ON d.product_id = p.product_id WHERE d.ticket_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ticketId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    InventoryTicketDetail d = new InventoryTicketDetail();
                    d.setDetailId(rs.getInt("detail_id"));
                    d.setTicketId(rs.getInt("ticket_id"));
                    d.setProductId(rs.getInt("product_id"));
                    d.setQuantity(rs.getInt("quantity"));
                    d.setActionType(rs.getString("action_type"));
                    d.setProductName(rs.getString("product_name"));
                    details.add(d);
                }
            }
        }
        return details;
    }

    public void approveTransferTicket(int ticketId, int approvedByUserId) throws SQLException {
        InventoryTicket ticket = findById(ticketId);
        if (ticket == null || !ticket.getStatus().equals("PENDING")) {
            throw new SQLException("Phiếu không hợp lệ hoặc đã được xử lý.");
        }
        List<InventoryTicketDetail> details = getTicketDetails(ticketId);
        
        String updateTicketSql = "UPDATE inventory_ticket SET status = 'IN_TRANSIT' WHERE ticket_id = ?";
        
        try (Connection conn = DBContext.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(updateTicketSql)) {
                stmt.setInt(1, ticketId);
                stmt.executeUpdate();
            }
        }
    }

    public void cancelTicket(int ticketId, int userId, String reason) throws SQLException {
        String sql = "UPDATE inventory_ticket SET status = 'CANCELLED', note = CONCAT(ISNULL(note,''), ?) WHERE ticket_id = ? AND status = 'PENDING'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "\nHủy bởi UID " + userId + (reason != null && !reason.isEmpty() ? ": " + reason : ""));
            stmt.setInt(2, ticketId);
            stmt.executeUpdate();
        }
    }

    public void rejectTicket(int ticketId, int userId, String reason) throws SQLException {
        String sql = "UPDATE inventory_ticket SET status = 'REJECTED', note = CONCAT(ISNULL(note,''), ?) WHERE ticket_id = ? AND status = 'PENDING'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "\nTừ chối bởi UID " + userId + (reason != null && !reason.isEmpty() ? ": " + reason : ""));
            stmt.setInt(2, ticketId);
            stmt.executeUpdate();
        }
    }

    public void confirmDispatch(int ticketId, int userId) throws SQLException {
        InventoryTicket ticket = findById(ticketId);
        if (ticket == null || !ticket.getStatus().equals("IN_TRANSIT") || ticket.isExportedBySender()) {
            throw new SQLException("Phiếu không hợp lệ hoặc đã xuất kho.");
        }
        
        List<InventoryTicketDetail> details = getTicketDetails(ticketId);
        String getInventorySql = "SELECT quantity_in_stock FROM inventory WHERE warehouse_id = ? AND product_id = ?";
        String updateInventorySql = "UPDATE inventory SET quantity_in_stock = quantity_in_stock - ? WHERE warehouse_id = ? AND product_id = ?";
        String insertTxSql = "INSERT INTO stock_transaction (warehouse_id, product_id, reference_type, reference_id, transaction_type, quantity, before_quantity, after_quantity, note, created_by) VALUES (?, ?, 'TRANSFER', ?, 'EXPORT', ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update InventoryTicket status
                String updateTicketSql = "UPDATE inventory_ticket SET is_exported_by_sender = 1";
                boolean completeIt = ticket.isImportedByReceiver();
                if (completeIt) {
                    updateTicketSql += ", status = 'COMPLETED'";
                }
                updateTicketSql += " WHERE ticket_id = ?";
                try (PreparedStatement s = conn.prepareStatement(updateTicketSql)) {
                    s.setInt(1, ticketId);
                    s.executeUpdate();
                }
                
                // Cập nhật actual_quantity = quantity cho tất cả chi tiết của phiếu TX này
                String updateDetailSql = "UPDATE inventory_ticket_detail SET actual_quantity = quantity WHERE ticket_id = ?";
                try (PreparedStatement s = conn.prepareStatement(updateDetailSql)) {
                    s.setInt(1, ticketId);
                    s.executeUpdate();
                }

                // Trừ tồn kho và ghi log stock_transaction
                for (InventoryTicketDetail d : details) {
                    int senderWId = ticket.getFromWarehouseId();
                    
                    int beforeQty = 0;
                    try (PreparedStatement s = conn.prepareStatement(getInventorySql)) {
                        s.setInt(1, senderWId);
                        s.setInt(2, d.getProductId());
                        try (ResultSet r = s.executeQuery()) {
                            if (r.next()) beforeQty = r.getInt(1);
                        }
                    }
                    
                    if (beforeQty < d.getQuantity()) {
                        throw new SQLException("Không đủ tồn kho để xuất sản phẩm ID=" + d.getProductId() + " (cần " + d.getQuantity() + ", còn " + beforeQty + ")");
                    }
                    
                    try (PreparedStatement s = conn.prepareStatement(updateInventorySql)) {
                        s.setInt(1, d.getQuantity());
                        s.setInt(2, senderWId);
                        s.setInt(3, d.getProductId());
                        s.executeUpdate();
                    }
                    
                    try (PreparedStatement s = conn.prepareStatement(insertTxSql)) {
                        s.setInt(1, senderWId);
                        s.setInt(2, d.getProductId());
                        s.setInt(3, ticketId);
                        s.setInt(4, d.getQuantity());
                        s.setInt(5, beforeQty);
                        s.setInt(6, beforeQty - d.getQuantity());
                        s.setString(7, "Xác nhận xuất kho");
                        s.setInt(8, userId);
                        s.executeUpdate();
                    }
                }
                
                if (completeIt) {
                    checkAndCompleteOriginalTransferRequest(ticket.getTicketCode(), conn);
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void rejectDispatch(int ticketId, int userId, String note) throws SQLException {
        InventoryTicket ticket = findById(ticketId);
        if (ticket == null || !ticket.getStatus().equals("IN_TRANSIT") || ticket.isExportedBySender()) {
            throw new SQLException("Phiếu không hợp lệ hoặc đã xuất kho.");
        }
        String updateSql = "UPDATE inventory_ticket SET status = 'REJECTED', note = ? WHERE ticket_id = ?";
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, "Từ chối xuất kho: " + (note != null ? note : ""));
                stmt.setInt(2, ticketId);
                stmt.executeUpdate();
            }
            checkAndCompleteOriginalTransferRequest(ticket.getTicketCode(), conn);
            conn.commit();
        }
    }

    public void confirmReceiptWithDiscrepancy(int ticketId, int userId, String note, java.util.Map<Integer, Integer> actualQtys, int currentWarehouseId) throws SQLException {
        InventoryTicket ticket = findById(ticketId);
        if (ticket == null || !ticket.getStatus().equals("IN_TRANSIT") || ticket.isImportedByReceiver()) {
            throw new SQLException("Phiếu không hợp lệ hoặc đã nhập kho.");
        }
        
        List<InventoryTicketDetail> details = getTicketDetails(ticketId);
        String getInventorySql = "SELECT quantity_in_stock FROM inventory WHERE warehouse_id = ? AND product_id = ?";
        String updateInventorySql = "UPDATE inventory SET quantity_in_stock = quantity_in_stock + ? WHERE warehouse_id = ? AND product_id = ?";
        String insertInventorySql = "INSERT INTO inventory (warehouse_id, product_id, quantity_in_stock, status, updated_at) VALUES (?, ?, ?, 'ACTIVE', GETDATE())";
        String insertTxSql = "INSERT INTO stock_transaction (warehouse_id, product_id, reference_type, reference_id, transaction_type, quantity, before_quantity, after_quantity, note, created_by) VALUES (?, ?, 'TRANSFER', ?, 'IMPORT', ?, ?, ?, ?, ?)";
        String updateTicketSql = "UPDATE inventory_ticket SET is_imported_by_receiver = 1, note = CONCAT(ISNULL(note, ''), ?) ";
        
        boolean hasDiscrepancy = false;
        List<InventoryTicketDetail> discrepancyDetails = new java.util.ArrayList<>();

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (InventoryTicketDetail d : details) {
                    int receivingWId = ticket.getToWarehouseId();
                    if (receivingWId != currentWarehouseId) continue; // Only process items meant for this receiver
                    
                    int expectedQty = d.getQuantity();
                    int actualQty = actualQtys.getOrDefault(d.getProductId(), expectedQty);
                    
                    if (actualQty != expectedQty) {
                        hasDiscrepancy = true;
                        InventoryTicketDetail disc = new InventoryTicketDetail();
                        disc.setProductId(d.getProductId());
                        disc.setQuantity(actualQty - expectedQty); // Negative means loss, positive means extra
                        disc.setActionType("DISCREPANCY");
                        discrepancyDetails.add(disc);
                    }
                    
                    if (actualQty > 0) {
                        int beforeReceive = 0;
                        boolean existsReceive = false;
                        try (PreparedStatement s = conn.prepareStatement(getInventorySql)) {
                            s.setInt(1, receivingWId);
                            s.setInt(2, d.getProductId());
                            try (ResultSet r = s.executeQuery()) {
                                if (r.next()) {
                                    beforeReceive = r.getInt(1);
                                    existsReceive = true;
                                }
                            }
                        }
                        if (existsReceive) {
                            try (PreparedStatement s = conn.prepareStatement(updateInventorySql)) {
                                s.setInt(1, actualQty);
                                s.setInt(2, receivingWId);
                                s.setInt(3, d.getProductId());
                                s.executeUpdate();
                            }
                        } else {
                            try (PreparedStatement s = conn.prepareStatement(insertInventorySql)) {
                                s.setInt(1, receivingWId);
                                s.setInt(2, d.getProductId());
                                s.setInt(3, actualQty);
                                s.executeUpdate();
                            }
                        }
                        try (PreparedStatement s = conn.prepareStatement(insertTxSql)) {
                            s.setInt(1, receivingWId);
                            s.setInt(2, d.getProductId());
                            s.setInt(3, ticketId);
                            s.setInt(4, actualQty);
                            s.setInt(5, beforeReceive);
                            s.setInt(6, beforeReceive + actualQty);
                            s.setString(7, "Xác nhận nhập kho" + (actualQty != expectedQty ? " (Có chênh lệch)" : ""));
                            s.setInt(8, userId);
                            s.executeUpdate();
                        }
                    }
                }
                
                // If sender already confirmed, complete it
                boolean completeIt = ticket.isExportedBySender();
                if (completeIt) {
                    updateTicketSql += ", status = '" + (hasDiscrepancy ? "COMPLETED_WITH_ERROR" : "COMPLETED") + "' ";
                }
                updateTicketSql += "WHERE ticket_id = ?";
                
                try (PreparedStatement s = conn.prepareStatement(updateTicketSql)) {
                    s.setString(1, note != null ? "\nKho nhận: " + note : "");
                    s.setInt(2, ticketId);
                    s.executeUpdate();
                }
                
                // Create Discrepancy Ticket if needed
                if (hasDiscrepancy) {
                    InventoryTicket discTicket = new InventoryTicket();
                    discTicket.setTicketCode("ERR-" + System.currentTimeMillis());
                    discTicket.setTicketType("DISCREPANCY");
                    discTicket.setFromWarehouseId(currentWarehouseId); // The warehouse that found the issue
                    discTicket.setToWarehouseId(currentWarehouseId); // Same warehouse to avoid NOT NULL constraint
                    discTicket.setStatus("PENDING");
                    discTicket.setCreatedBy(userId);
                    discTicket.setNote("Lệch từ phiếu chuyển: " + ticket.getTicketCode());
                    createExchangeTicket(conn, discTicket, discrepancyDetails); // Reuse existing, need a transactional version
                }
                
                if (completeIt) {
                    checkAndCompleteOriginalTransferRequest(ticket.getTicketCode(), conn);
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void createExchangeTicket(Connection conn, InventoryTicket ticket, List<InventoryTicketDetail> details) throws SQLException {
        String sql = "INSERT INTO inventory_ticket (ticket_code, ticket_type, from_warehouse_id, to_warehouse_id, status, created_by, note) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int newTicketId = 0;
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, ticket.getTicketCode());
            stmt.setString(2, ticket.getTicketType());
            if (ticket.getFromWarehouseId() != null) {
                stmt.setInt(3, ticket.getFromWarehouseId());
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            if (ticket.getToWarehouseId() != null) {
                stmt.setInt(4, ticket.getToWarehouseId());
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER);
            }
            stmt.setString(5, ticket.getStatus());
            stmt.setInt(6, ticket.getCreatedBy());
            stmt.setString(7, ticket.getNote());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) newTicketId = rs.getInt(1);
            }
        }
        
        String dSql = "INSERT INTO inventory_ticket_detail (ticket_id, product_id, quantity, action_type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement dStmt = conn.prepareStatement(dSql)) {
            for (InventoryTicketDetail d : details) {
                dStmt.setInt(1, newTicketId);
                dStmt.setInt(2, d.getProductId());
                dStmt.setInt(3, d.getQuantity());
                dStmt.setString(4, d.getActionType());
                dStmt.addBatch();
            }
            dStmt.executeBatch();
        }
    }
    private void checkAndCompleteOriginalTransferRequest(String currentTicketCode, Connection conn) throws SQLException {
        if (currentTicketCode == null || (!currentTicketCode.startsWith("TX-") && !currentTicketCode.startsWith("TI-"))) return;
        try {
            int originalTicketId = Integer.parseInt(currentTicketCode.substring(3));
            String sql = "SELECT status FROM inventory_ticket WHERE ticket_type = 'TRANSFER_CHECK' AND (ticket_code = ? OR ticket_code = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "TX-" + originalTicketId);
                stmt.setString(2, "TI-" + originalTicketId);
                try (ResultSet rs = stmt.executeQuery()) {
                    boolean allCompletedOrRejected = true;
                    boolean hasError = false;
                    boolean hasRejected = false;
                    boolean foundAny = false;
                    while (rs.next()) {
                        foundAny = true;
                        String status = rs.getString("status");
                        if (!status.startsWith("COMPLETED") && !"REJECTED".equals(status) && !"CANCELLED".equals(status)) {
                            allCompletedOrRejected = false;
                            break;
                        }
                        if ("COMPLETED_WITH_ERROR".equals(status)) hasError = true;
                        if ("REJECTED".equals(status) || "CANCELLED".equals(status)) hasRejected = true;
                    }
                    if (foundAny && allCompletedOrRejected) {
                        String finalStatus = "COMPLETED";
                        if (hasRejected) finalStatus = "REJECTED";
                        else if (hasError) finalStatus = "COMPLETED_WITH_ERROR";
                        
                        String updateSql = "UPDATE inventory_ticket SET status = ? WHERE ticket_id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, finalStatus);
                            updateStmt.setInt(2, originalTicketId);
                            updateStmt.executeUpdate();
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            // ignore if not our specific format
        }
    }
    
    public String getTransferProgress(int parentTicketId) throws SQLException {
        String sql = "SELECT ticket_code, status FROM inventory_ticket WHERE ticket_type = 'TRANSFER_CHECK' AND (ticket_code = ? OR ticket_code = ?)";
        boolean hasTx = false;
        boolean hasTi = false;
        String txStatus = "";
        String tiStatus = "";
        
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "TX-" + parentTicketId);
            stmt.setString(2, "TI-" + parentTicketId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString("ticket_code");
                    String st = rs.getString("status");
                    if (code.startsWith("TX-")) {
                        hasTx = true;
                        txStatus = st;
                    } else if (code.startsWith("TI-")) {
                        hasTi = true;
                        tiStatus = st;
                    }
                }
            }
        }
        
        if (!hasTx && !hasTi) return "Chờ xử lý";
        
        boolean txDone = "COMPLETED".equals(txStatus) || "COMPLETED_WITH_ERROR".equals(txStatus);
        boolean tiDone = "COMPLETED".equals(tiStatus) || "COMPLETED_WITH_ERROR".equals(tiStatus);
        
        if (txDone && tiDone) return "Hoàn tất 2 bên";
        if (txDone) return "Bên gửi đã xuất, chờ bên nhận";
        if (tiDone) return "Bên nhận đã nhập, chờ bên gửi xuất";
        
        return "Đang chờ 2 bên xử lý";
    }

    public InventoryTicket findByCode(String code) throws SQLException {
        String sql = "SELECT t.*, " +
                     "w1.warehouse_name as from_warehouse_name, " +
                     "w2.warehouse_name as to_warehouse_name, " +
                     "e.fullName as created_by_name " +
                     "FROM inventory_ticket t " +
                     "LEFT JOIN warehouse w1 ON t.from_warehouse_id = w1.warehouse_id " +
                     "LEFT JOIN warehouse w2 ON t.to_warehouse_id = w2.warehouse_id " +
                      "LEFT JOIN Employee e ON t.created_by = e.emp_id " +
                      "WHERE t.ticket_code = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractTicket(rs);
                }
            }
        }
        return null;
    }
}
