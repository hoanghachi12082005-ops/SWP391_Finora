package dao;

import model.Role;
import util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO skeleton for table Role. TODO: Implement full CRUD and workflow-specific queries. */
public class RoleDAO {
    private static final String SELECT_COLUMNS = "RoleID,Name,Description";
    public List<Role> findAll() throws SQLException {
        List<Role> items = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Role";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) items.add(extractRole(resultSet));
        }
        return items;
    }
    public Role findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Role WHERE RoleID = ?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? extractRole(resultSet) : null; }
        }
    }
    private Role extractRole(ResultSet resultSet) throws SQLException {
        Role item = new Role();
        // TODO: Map ResultSet columns from DBFinora.sql to Role fields.
        return item;
    }
}
