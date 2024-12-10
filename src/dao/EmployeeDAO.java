package dao;

import models.Employee;
import java.sql.*;

public class EmployeeDAO {
    private Connection connection;

    public EmployeeDAO(Connection connection) {
        this.connection = connection;
    }

    // Method to authenticate employee based on username and password
    public Employee authenticate(String username, String password) {
        Employee employee = null;
        try {
            // Query to authenticate against the 'users' table
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password); // Note: In production, you should hash the password

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // Fetch user information
                int id = resultSet.getInt("id");
                String role = resultSet.getString("role");

                // Return the employee with the role info
                employee = new Employee(id, username, password, role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employee;
    }
}
