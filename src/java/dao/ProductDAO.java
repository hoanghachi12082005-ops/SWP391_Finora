package dao;

import model.Product;
import util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO skeleton for table Product. TODO: Implement full CRUD and workflow-specific queries. */
public class ProductDAO {
    private static final String SELECT_COLUMNS = "ProductID,CategoryID,Name,SKU,Price,CostPrice,StockAlertQty,Status,CreatedAt";
    public List<Product> findAll() throws SQLException {
        List<Product> items = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Product";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) items.add(extractProduct(resultSet));
        }
        return items;
    }
    public Product findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Product WHERE ProductID = ?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? extractProduct(resultSet) : null; }
        }
    }
    private Product extractProduct(ResultSet resultSet) throws SQLException {
        Product item = new Product();
        // TODO: Map ResultSet columns from DBFinora.sql to Product fields.
        return item;
    }
}
