package dao;

import model.Supplier;
import util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO skeleton for table Supplier. TODO: Implement full CRUD and workflow-specific queries. */
public class SupplierDAO {
    private static final String SELECT_COLUMNS = "SupplierID,Name,Phone,Email,Address,Status,CreatedAt";
    public List<Supplier> findAll() throws SQLException {
        List<Supplier> items = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Supplier";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) items.add(extractSupplier(resultSet));
        }
        return items;
    }
    public Supplier findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Supplier WHERE SupplierID = ?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? extractSupplier(resultSet) : null; }
        }
    }
    private Supplier extractSupplier(ResultSet resultSet) throws SQLException {
        Supplier item = new Supplier();
        // TODO: Map ResultSet columns from DBFinora.sql to Supplier fields.
        return item;
    }
}
