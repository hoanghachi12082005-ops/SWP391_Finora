package dao;

import model.StockTransfer;
import util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO skeleton for table StockTransfer. TODO: Implement full CRUD and workflow-specific queries. */
public class StockTransferDAO {
    private static final String SELECT_COLUMNS = "StockTransferID,BranchID,EmployeeID,ProductID,FromWarehouseID,ToWarehouseID,TransferCode,TransferDate,Quantity,Status,Note,CreatedAt";
    public List<StockTransfer> findAll() throws SQLException {
        List<StockTransfer> items = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM StockTransfer";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) items.add(extractStockTransfer(resultSet));
        }
        return items;
    }
    public StockTransfer findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM StockTransfer WHERE StockTransferID = ?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? extractStockTransfer(resultSet) : null; }
        }
    }
    private StockTransfer extractStockTransfer(ResultSet resultSet) throws SQLException {
        StockTransfer item = new StockTransfer();
        // TODO: Map ResultSet columns from DBFinora.sql to StockTransfer fields.
        return item;
    }
}
