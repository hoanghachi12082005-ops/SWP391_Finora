package dao;

import model.AuditLog;
import util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO skeleton for table AuditLog. TODO: Implement full CRUD and workflow-specific queries. */
public class AuditLogDAO {
    private static final String SELECT_COLUMNS = "AuditLogID,EmployeeID,Action,EntityName,EntityID,OldData,NewData,CreatedAt";
    public List<AuditLog> findAll() throws SQLException {
        List<AuditLog> items = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM AuditLog";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) items.add(extractAuditLog(resultSet));
        }
        return items;
    }
    public AuditLog findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM AuditLog WHERE AuditLogID = ?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? extractAuditLog(resultSet) : null; }
        }
    }
    private AuditLog extractAuditLog(ResultSet resultSet) throws SQLException {
        AuditLog item = new AuditLog();
        // TODO: Map ResultSet columns from DBFinora.sql to AuditLog fields.
        return item;
    }
}
