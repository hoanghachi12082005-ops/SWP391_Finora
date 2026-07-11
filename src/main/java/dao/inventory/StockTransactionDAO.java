package dao.inventory;

import model.StockTransaction;
import util.database.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StockTransactionDAO {

    public List<StockTransaction> findAll(int warehouseId, List<Integer> allowedWarehouseIds, int offset, int limit, String typeFilter, String dateFilter) throws SQLException {
        List<StockTransaction> transactions = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT " +
            "    MIN(st.stock_transaction_id) as stock_transaction_id, " +
            "    st.warehouse_id, " +
            "    MIN(st.product_id) as product_id, " +
            "    st.reference_id, " +
            "    MIN(st.reference_type) as reference_type, " +
            "    MIN(st.transaction_type) as transaction_type, " +
            "    SUM(st.quantity) as quantity, " +
            "    0 as before_quantity, " +
            "    0 as after_quantity, " +
            "    MIN(st.note) as note, " +
            "    MIN(st.created_by) as created_by, " +
            "    MIN(st.created_at) as created_at, " +
            "    STRING_AGG(p.product_name, ', ') as product_name, " +
            "    '' as product_codebar, " +
            "    MIN(e.fullName) as created_by_name, " +
            "    MIN(w.warehouse_name) as warehouse_name " +
            "FROM stock_transaction st " +
            "JOIN product p ON st.product_id = p.product_id " +
            "JOIN warehouse w ON st.warehouse_id = w.warehouse_id " +
            "LEFT JOIN Employee e ON st.created_by = e.emp_id " +
            "WHERE 1=1"
        );

        if (warehouseId > 0) {
            sql.append(" AND st.warehouse_id = ?");
        } else if (allowedWarehouseIds != null && !allowedWarehouseIds.isEmpty()) {
            sql.append(" AND st.warehouse_id IN (");
            for (int i = 0; i < allowedWarehouseIds.size(); i++) {
                sql.append(i > 0 ? ",?" : "?");
            }
            sql.append(")");
        } else {
            // No access to any warehouse, return empty immediately
            return transactions;
        }

        if (typeFilter != null && !typeFilter.trim().isEmpty()) {
            sql.append(" AND st.transaction_type = ?");
        }
        // Basic date filter support
        if (dateFilter != null && dateFilter.equals("today")) {
            sql.append(" AND CAST(st.created_at AS DATE) = CAST(GETDATE() AS DATE)");
        }

        sql.append(" GROUP BY st.warehouse_id, st.reference_type, st.reference_id");
        sql.append(" ORDER BY MIN(st.created_at) DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            if (warehouseId > 0) {
                stmt.setInt(idx++, warehouseId);
            } else if (allowedWarehouseIds != null && !allowedWarehouseIds.isEmpty()) {
                for (Integer wid : allowedWarehouseIds) stmt.setInt(idx++, wid);
            }
            if (typeFilter != null && !typeFilter.trim().isEmpty()) stmt.setString(idx++, typeFilter);
            stmt.setInt(idx++, offset);
            stmt.setInt(idx, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(extractTransaction(rs));
                }
            }
        }
        return transactions;
    }
    public List<StockTransaction> findByReference(String referenceType, int referenceId) throws SQLException {
        List<StockTransaction> transactions = new ArrayList<>();
        String sql = "SELECT st.*, p.product_name, p.product_codebar, " +
                     "e.fullName as created_by_name, w.warehouse_name " +
                     "FROM stock_transaction st " +
                     "JOIN product p ON st.product_id = p.product_id " +
                     "JOIN warehouse w ON st.warehouse_id = w.warehouse_id " +
                     "LEFT JOIN Employee e ON st.created_by = e.emp_id " +
                     "WHERE st.reference_type = ? AND st.reference_id = ?";
                     
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, referenceType);
            stmt.setInt(2, referenceId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(extractTransaction(rs));
                }
            }
        }
        return transactions;
    }

    public void insert(StockTransaction tx) throws SQLException {
        String sql = "INSERT INTO stock_transaction (warehouse_id, product_id, reference_type, reference_id, transaction_type, quantity, before_quantity, after_quantity, note, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tx.getWarehouseId());
            stmt.setInt(2, tx.getProductId());
            stmt.setString(3, tx.getReferenceType());
            if (tx.getReferenceId() != null) {
                stmt.setInt(4, tx.getReferenceId());
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER);
            }
            stmt.setString(5, tx.getTransactionType());
            stmt.setInt(6, tx.getQuantity());
            stmt.setInt(7, tx.getBeforeQuantity());
            stmt.setInt(8, tx.getAfterQuantity());
            stmt.setString(9, tx.getNote());
            stmt.setInt(10, tx.getCreatedBy());
            stmt.executeUpdate();
        }
    }

    private StockTransaction extractTransaction(ResultSet rs) throws SQLException {
        StockTransaction tx = new StockTransaction();
        tx.setStockTransactionId(rs.getInt("stock_transaction_id"));
        tx.setWarehouseId(rs.getInt("warehouse_id"));
        tx.setProductId(rs.getInt("product_id"));
        tx.setReferenceType(rs.getString("reference_type"));
        if (rs.getObject("reference_id") != null) {
            tx.setReferenceId(rs.getInt("reference_id"));
        }
        tx.setTransactionType(rs.getString("transaction_type"));
        tx.setQuantity(rs.getInt("quantity"));
        tx.setBeforeQuantity(rs.getInt("before_quantity"));
        tx.setAfterQuantity(rs.getInt("after_quantity"));
        tx.setNote(rs.getString("note"));
        tx.setCreatedBy(rs.getInt("created_by"));
        if (rs.getTimestamp("created_at") != null) {
            tx.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        tx.setProductName(rs.getString("product_name"));
        tx.setProductCodebar(rs.getString("product_codebar"));
        tx.setCreatedByName(rs.getString("created_by_name"));
        tx.setWarehouseName(rs.getString("warehouse_name"));
        return tx;
    }
}
