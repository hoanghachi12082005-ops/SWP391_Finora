package finance.dao;

import finance.model.FinanceTransaction;
import common.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO skeleton for table FinanceTransaction. TODO: Implement full CRUD and workflow-specific queries. */
public class FinanceTransactionDAO {
    private static final String SELECT_COLUMNS = "TransactionID,BranchID,EmployeeID,TransactionCode,TransactionDate,TransactionType,Amount,ReferenceID,ReferenceType,Note,CreatedAt";
    public List<FinanceTransaction> findAll() throws SQLException {
        List<FinanceTransaction> items = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM FinanceTransaction";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) items.add(extractFinanceTransaction(resultSet));
        }
        return items;
    }
    public FinanceTransaction findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM FinanceTransaction WHERE TransactionID = ?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? extractFinanceTransaction(resultSet) : null; }
        }
    }
    private FinanceTransaction extractFinanceTransaction(ResultSet resultSet) throws SQLException {
        FinanceTransaction item = new FinanceTransaction();
        // TODO: Map ResultSet columns from DBFinora.sql to FinanceTransaction fields.
        return item;
    }
}
