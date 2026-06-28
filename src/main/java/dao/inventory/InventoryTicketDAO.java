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
        String sql = "SELECT t.*, " +
                     "fw.warehouse_name as from_warehouse_name, " +
                     "tw.warehouse_name as to_warehouse_name, " +
                     "e.fullName as created_by_name " +
                     "FROM inventory_ticket t " +
                     "LEFT JOIN warehouse fw ON t.from_warehouse_id = fw.warehouse_id " +
                     "LEFT JOIN warehouse tw ON t.to_warehouse_id = tw.warehouse_id " +
                     "LEFT JOIN Employee e ON t.created_by = e.EmployeeID " +
                     "WHERE t.ticket_type = ? ";
        
        if (warehouseId != null && warehouseId > 0) {
            sql += " AND (t.from_warehouse_id = " + warehouseId + " OR t.to_warehouse_id = " + warehouseId + ") ";
        }
        sql += " ORDER BY t.created_at DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ticketType);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(extractTicket(rs));
                }
            }
        }
        return tickets;
    }

    public int getPendingCount(String ticketType, Integer warehouseId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM inventory_ticket WHERE status = 'PENDING'";
        if (ticketType != null) {
            sql += " AND ticket_type = '" + ticketType + "'";
        }
        if (warehouseId != null && warehouseId > 0) {
            sql += " AND (from_warehouse_id = " + warehouseId + " OR to_warehouse_id = " + warehouseId + ")";
        }
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
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
                     "LEFT JOIN Employee e ON t.created_by = e.EmployeeID " +
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

    public List<InventoryTicketDetail> getTicketDetails(int ticketId) throws SQLException {
        List<InventoryTicketDetail> details = new ArrayList<>();
        String sql = "SELECT d.*, p.Name as product_name FROM inventory_ticket_detail d JOIN Product p ON d.product_id = p.ProductID WHERE d.ticket_id = ?";
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
        
        String updateTicketSql = "UPDATE inventory_ticket SET status = 'COMPLETED' WHERE ticket_id = ?";
        String getInventorySql = "SELECT quantity_in_stock FROM inventory WHERE warehouse_id = ? AND product_id = ?";
        String updateInventorySql = "UPDATE inventory SET quantity_in_stock = quantity_in_stock + ? WHERE warehouse_id = ? AND product_id = ?";
        String insertInventorySql = "INSERT INTO inventory (warehouse_id, product_id, quantity_in_stock, min_stock_level, max_stock_level) VALUES (?, ?, ?, 0, 999999)";
        String insertTxSql = "INSERT INTO stock_transaction (warehouse_id, product_id, reference_type, reference_id, transaction_type, quantity, before_quantity, after_quantity, note, created_by) VALUES (?, ?, 'TRANSFER', ?, ?, ?, ?, ?, 'Duyệt phiếu Đ/C', ?)";
        
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // update ticket status
                try (PreparedStatement stmt = conn.prepareStatement(updateTicketSql)) {
                    stmt.setInt(1, ticketId);
                    stmt.executeUpdate();
                }
                
                for (InventoryTicketDetail d : details) {
                    int qty = d.getQuantity();
                    String action = d.getActionType();
                    int fromW = ticket.getFromWarehouseId(); // Creator
                    int toW = ticket.getToWarehouseId(); // Partner
                    
                    int sendingWId = "SEND".equals(action) ? fromW : toW;
                    int receivingWId = "SEND".equals(action) ? toW : fromW;
                    
                    // --- Process Sending Warehouse ---
                    int beforeSend = 0;
                    try (PreparedStatement s = conn.prepareStatement(getInventorySql)) {
                        s.setInt(1, sendingWId);
                        s.setInt(2, d.getProductId());
                        try (ResultSet r = s.executeQuery()) {
                            if (r.next()) beforeSend = r.getInt(1);
                        }
                    }
                    if (beforeSend < qty) {
                        throw new SQLException("Tồn kho không đủ cho sản phẩm " + d.getProductName() + " (cần " + qty + ", còn " + beforeSend + ").");
                    }
                    try (PreparedStatement s = conn.prepareStatement(updateInventorySql)) {
                        s.setInt(1, -qty);
                        s.setInt(2, sendingWId);
                        s.setInt(3, d.getProductId());
                        s.executeUpdate();
                    }
                    try (PreparedStatement s = conn.prepareStatement(insertTxSql)) {
                        s.setInt(1, sendingWId);
                        s.setInt(2, d.getProductId());
                        s.setInt(3, ticketId);
                        s.setString(4, "EXPORT");
                        s.setInt(5, qty);
                        s.setInt(6, beforeSend);
                        s.setInt(7, beforeSend - qty);
                        s.setInt(8, approvedByUserId);
                        s.executeUpdate();
                    }
                    
                    // --- Process Receiving Warehouse ---
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
                            s.setInt(1, qty);
                            s.setInt(2, receivingWId);
                            s.setInt(3, d.getProductId());
                            s.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement s = conn.prepareStatement(insertInventorySql)) {
                            s.setInt(1, receivingWId);
                            s.setInt(2, d.getProductId());
                            s.setInt(3, qty);
                            s.executeUpdate();
                        }
                    }
                    try (PreparedStatement s = conn.prepareStatement(insertTxSql)) {
                        s.setInt(1, receivingWId);
                        s.setInt(2, d.getProductId());
                        s.setInt(3, ticketId);
                        s.setString(4, "IMPORT");
                        s.setInt(5, qty);
                        s.setInt(6, beforeReceive);
                        s.setInt(7, beforeReceive + qty);
                        s.setInt(8, approvedByUserId);
                        s.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
