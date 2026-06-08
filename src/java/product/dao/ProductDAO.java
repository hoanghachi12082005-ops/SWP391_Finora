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
        List<Product> items = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Product ORDER BY ProductID ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, offset);
            statement.setInt(2, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(extractProduct(resultSet));
                }
            }
        }
        return items;
    }

    public int getTotalCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Product";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }

    public Product findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Product WHERE ProductID = ?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { 
                return resultSet.next() ? extractProduct(resultSet) : null; 
            }
        }
    }

    public void insert(Product product) throws SQLException {
        String sql = "INSERT INTO Product (CategoryID, Name, SKU, Price, CostPrice, StockAlertQty, Status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, product.getCategoryID());
            statement.setString(2, product.getName());
            statement.setString(3, product.getSku());
            statement.setBigDecimal(4, product.getPrice());
            statement.setBigDecimal(5, product.getCostPrice());
            statement.setInt(6, product.getStockAlertQty());
            statement.setString(7, product.getStatus());
            statement.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Product WHERE ProductID = ?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private Product extractProduct(ResultSet resultSet) throws SQLException {
        Product item = new Product();
        item.setProductID(resultSet.getInt("ProductID"));
        item.setCategoryID(resultSet.getInt("CategoryID"));
        item.setName(resultSet.getString("Name"));
        item.setSku(resultSet.getString("SKU"));
        item.setPrice(resultSet.getBigDecimal("Price"));
        item.setCostPrice(resultSet.getBigDecimal("CostPrice"));
        item.setStockAlertQty(resultSet.getInt("StockAlertQty"));
        item.setStatus(resultSet.getString("Status"));
        Timestamp createdAtTs = resultSet.getTimestamp("CreatedAt");
        if (createdAtTs != null) {
            item.setCreatedAt(createdAtTs.toLocalDateTime());
        }
        return item;
    }
}
