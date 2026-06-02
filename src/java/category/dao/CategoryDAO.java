package category.dao;

import category.model.Category;
import common.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO skeleton for table Category. TODO: Implement full CRUD and workflow-specific queries. */
public class CategoryDAO {
    private static final String SELECT_COLUMNS = "CategoryID,Name,Description,ParentID,Status";
    public List<Category> findAll() throws SQLException {
        List<Category> items = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Category";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) items.add(extractCategory(resultSet));
        }
        return items;
    }
    public Category findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Category WHERE CategoryID = ?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? extractCategory(resultSet) : null; }
        }
    }
    private Category extractCategory(ResultSet resultSet) throws SQLException {
        Category item = new Category();
        // TODO: Map ResultSet columns from DBFinora.sql to Category fields.
        return item;
    }
}
