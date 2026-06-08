package order.dao;

import order.model.Order;
import common.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO skeleton for table Orders. TODO: Implement full CRUD and workflow-specific queries. */
public class OrderDAO {
    private static final String SELECT_COLUMNS = "OrderID,BranchID,EmployeeID,CustomerID,SupplierID,OrderCode,OrderType,Subtotal,DiscountAmount,TotalAmount,Status,CreatedAt";
    public List<Order> findAll() throws SQLException {
        List<Order> items = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Orders";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) items.add(extractOrder(resultSet));
        }
        return items;
    }
    public Order findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Orders WHERE OrderID = ?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? extractOrder(resultSet) : null; }
        }
    }
    private Order extractOrder(ResultSet resultSet) throws SQLException {
        Order item = new Order();
        // TODO: Map ResultSet columns from DBFinora.sql to Order fields.
        return item;
    }
}
