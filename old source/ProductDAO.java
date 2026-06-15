package product.dao;

import product.model.Product;
import common.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private static final String SELECT_COLUMNS = "ProductID, CategoryID, Name, SKU, Price, CostPrice, StockAlertQty, Status, CreatedAt";

    public List<Product> findAll(int offset, int limit) throws SQLException {
        return findAll(offset, limit, null, null);
    }

    public List<Product> findAll(int offset, int limit, String keyword, String status) throws SQLException {
        List<Product> items = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT " + SELECT_COLUMNS + " FROM Product WHERE 1=1");
        if (keyword != null && !keyword.isBlank()) sql.append(" AND (Name LIKE ? OR SKU LIKE ?)");
        if (status != null && !status.isBlank())  sql.append(" AND Status = ?");
        sql.append(" ORDER BY ProductID ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword + "%";
                stmt.setString(idx++, like);
                stmt.setString(idx++, like);
            }
            if (status != null && !status.isBlank()) stmt.setString(idx++, status);
            stmt.setInt(idx++, offset);
            stmt.setInt(idx, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) items.add(extractProduct(rs));
            }
        }
        return items;
    }

    public int getTotalCount() throws SQLException {
        return getTotalCount(null, null);
    }

    public int getTotalCount(String keyword, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Product WHERE 1=1");
        if (keyword != null && !keyword.isBlank()) sql.append(" AND (Name LIKE ? OR SKU LIKE ?)");
        if (status != null && !status.isBlank())  sql.append(" AND Status = ?");

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword + "%";
                stmt.setString(idx++, like);
                stmt.setString(idx++, like);
            }
            if (status != null && !status.isBlank()) stmt.setString(idx, status);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public Product findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Product WHERE ProductID = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? extractProduct(rs) : null;
            }
        }
    }

    public void insert(Product product) throws SQLException {
        String sql = "INSERT INTO Product (CategoryID, Name, SKU, Price, CostPrice, StockAlertQty, Status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, product.getCategoryID());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getSku());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setBigDecimal(5, product.getCostPrice());
            stmt.setInt(6, product.getStockAlertQty());
            stmt.setString(7, product.getStatus());
            stmt.executeUpdate();
        }
    }

    public void update(Product product) throws SQLException {
        String sql = "UPDATE Product SET CategoryID=?, Name=?, SKU=?, Price=?, CostPrice=?, StockAlertQty=?, Status=? WHERE ProductID=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, product.getCategoryID());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getSku());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setBigDecimal(5, product.getCostPrice());
            stmt.setInt(6, product.getStockAlertQty());
            stmt.setString(7, product.getStatus());
            stmt.setInt(8, product.getProductID());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Product WHERE ProductID = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }


    private Product extractProduct(ResultSet rs) throws SQLException {
        Product item = new Product();
        item.setProductID(rs.getInt("ProductID"));
        item.setCategoryID(rs.getInt("CategoryID"));
        item.setName(rs.getString("Name"));
        item.setSku(rs.getString("SKU"));
        item.setPrice(rs.getBigDecimal("Price"));
        item.setCostPrice(rs.getBigDecimal("CostPrice"));
        item.setStockAlertQty(rs.getInt("StockAlertQty"));
        item.setStatus(rs.getString("Status"));
        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) item.setCreatedAt(ts.toLocalDateTime());
        return item;
    }
}


