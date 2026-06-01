package dao;

import model.OrderDetail;
import util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO skeleton for table OrderDetail. TODO: Implement full CRUD and workflow-specific queries. */
public class OrderDetailDAO {
    private static final String SELECT_COLUMNS = "OrderDetailID,OrderID,ProductID,Quantity,UnitPrice,Subtotal";
    public List<OrderDetail> findAll() throws SQLException {
        List<OrderDetail> items = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM OrderDetail";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) items.add(extractOrderDetail(resultSet));
        }
        return items;
    }
    public OrderDetail findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM OrderDetail WHERE OrderDetailID = ?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? extractOrderDetail(resultSet) : null; }
        }
    }
    private OrderDetail extractOrderDetail(ResultSet resultSet) throws SQLException {
        OrderDetail item = new OrderDetail();
        // TODO: Map ResultSet columns from DBFinora.sql to OrderDetail fields.
        return item;
    }
}
