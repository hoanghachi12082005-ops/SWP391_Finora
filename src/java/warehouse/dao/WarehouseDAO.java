package warehouse.dao;

import warehouse.model.Warehouse;
import common.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO skeleton for table Warehouse. TODO: Implement full CRUD and workflow-specific queries. */
public class WarehouseDAO {
    private static final String SELECT_COLUMNS = "WarehouseID,BranchID,EmployeeID,ProductID,Name,Address,Status,Quantity,AvailableQuantity,MinQuantity,MaxQuantity,UpdatedAt,CreatedAt";
    public List<Warehouse> findAll() throws SQLException {
        List<Warehouse> items = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Warehouse";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) items.add(extractWarehouse(resultSet));
        }
        return items;
    }
    public Warehouse findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Warehouse WHERE WarehouseID = ?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? extractWarehouse(resultSet) : null; }
        }
    }
    private Warehouse extractWarehouse(ResultSet resultSet) throws SQLException {
        Warehouse item = new Warehouse();
        // TODO: Map ResultSet columns from DBFinora.sql to Warehouse fields.
        return item;
    }
}
