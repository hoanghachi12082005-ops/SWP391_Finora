package employee.dao;

import employee.model.Employee;
import common.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO skeleton for table Employee. TODO: Implement full CRUD and workflow-specific queries. */
public class EmployeeDAO {
    private static final String SELECT_COLUMNS = "EmployeeID,RoleID,BranchID,FullName,Email,Phone,PasswordHash,Status,CreatedAt";
    public List<Employee> findAll() throws SQLException {
        List<Employee> items = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Employee";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) items.add(extractEmployee(resultSet));
        }
        return items;
    }
    public Employee findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Employee WHERE EmployeeID = ?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? extractEmployee(resultSet) : null; }
        }
    }
    private Employee extractEmployee(ResultSet resultSet) throws SQLException {
        Employee item = new Employee();
        // TODO: Map ResultSet columns from DBFinora.sql to Employee fields.
        return item;
    }
}
