package payment.dao;

import payment.model.Payment;
import common.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO skeleton for table Payments. TODO: Implement full CRUD and workflow-specific queries. */
public class PaymentDAO {
    private static final String SELECT_COLUMNS = "PaymentsID,OrderID,PaymentMethod,Amount,PaidAt,Reference,Status,CreatedAt";
    public List<Payment> findAll() throws SQLException {
        List<Payment> items = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Payments";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) items.add(extractPayment(resultSet));
        }
        return items;
    }
    public Payment findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Payments WHERE PaymentsID = ?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? extractPayment(resultSet) : null; }
        }
    }
    private Payment extractPayment(ResultSet resultSet) throws SQLException {
        Payment item = new Payment();
        // TODO: Map ResultSet columns from DBFinora.sql to Payment fields.
        return item;
    }
}
