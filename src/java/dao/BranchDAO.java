package dao;

import model.Branch;
import util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO skeleton for table Branch. TODO: Implement full CRUD and workflow-specific queries. */
public class BranchDAO {
    private static final String SELECT_COLUMNS = "BranchID,Name,Address,Phone,Status,CreatedAt";
    public List<Branch> findAll() throws SQLException {
        List<Branch> items = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Branch";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) items.add(extractBranch(resultSet));
        }
        return items;
    }
    public Branch findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Branch WHERE BranchID = ?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? extractBranch(resultSet) : null; }
        }
    }
    private Branch extractBranch(ResultSet resultSet) throws SQLException {
        Branch item = new Branch();
        // TODO: Map ResultSet columns from DBFinora.sql to Branch fields.
        return item;
    }
}
