package data_access_object;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import database.JDBCUtil;

public class LoginDAO {

    public String getUserRole(String username, String password) {

        try {
        	Connection connection = JDBCUtil.getConnection();

            String sql = "SELECT position FROM users WHERE username = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
            	return resultSet.getString("position"); // "admin" hoặc "staff"
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
