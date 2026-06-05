package dao;

import model.WarehouseTransaction;
import util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO skeleton for table WarehouseTransaction. TODO: Implement full CRUD and workflow-specific queries. */
public class WarehouseTransactionDAO {
    private static final String SELECT_COLUMNS = "WarehouseTransactionID,WarehouseID,ProductID,BeforeQuantity,Quantity,TransactionType,AfterQuantity,UnitCost,ReferenceType,ReferenceID,CreatedBy,CreatedAt";
    public List<WarehouseTransaction> findAll() throws SQLException {
        List<WarehouseTransaction> items = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM WarehouseTransaction";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) items.add(extractWarehouseTransaction(resultSet));
        }
        return items;
    }
    public WarehouseTransaction findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM WarehouseTransaction WHERE WarehouseTransactionID = ?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? extractWarehouseTransaction(resultSet) : null; }
        }
    }
    private WarehouseTransaction extractWarehouseTransaction(ResultSet resultSet) throws SQLException {
        WarehouseTransaction item = new WarehouseTransaction();
        // TODO: Map ResultSet columns from DBFinora.sql to WarehouseTransaction fields.
        return item;
    }
}
