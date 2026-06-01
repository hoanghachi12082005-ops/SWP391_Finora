package dao;

import model.Customer;
import util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO skeleton for table Customer. TODO: Implement full CRUD and workflow-specific queries. */
public class CustomerDAO {
    private static final String SELECT_COLUMNS = "CustomerID,FullName,Phone,Email,Address,DoB,Gender,MembershipTier,Points,CreatedAt";
    public List<Customer> findAll() throws SQLException {
        List<Customer> items = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Customer";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) items.add(extractCustomer(resultSet));
        }
        return items;
    }
    public Customer findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Customer WHERE CustomerID = ?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? extractCustomer(resultSet) : null; }
        }
    }
    private Customer extractCustomer(ResultSet resultSet) throws SQLException {
        Customer item = new Customer();
        // TODO: Map ResultSet columns from DBFinora.sql to Customer fields.
        return item;
    }
}
