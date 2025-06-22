package data_access_object;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import models.*;
import utils.*;

public class LoginDAO {
	
	public User getUserByUsername(String username) {
	    try {
	        Connection conn = JDBCUtil.getConnection();
	        String sql = "SELECT * FROM users WHERE username = ?";
	        PreparedStatement stmt = conn.prepareStatement(sql);
	        stmt.setString(1, username);

	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            User user = new User();
	            user.setUsername(rs.getString("username"));
	            user.setEmail(rs.getString("email"));
	            user.setEncryptedPassword(rs.getString("password")); // vẫn là mã hóa
	            user.setPosition(rs.getString("position"));
	            return user;
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
    
    public boolean registerUser(String username, String email, String encryptedPassword) {
        try {
            Connection conn = JDBCUtil.getConnection();
            String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, encryptedPassword);
            int rows = stmt.executeUpdate();

            JDBCUtil.closeStatement(stmt);
            JDBCUtil.closeConnection(conn);
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePasswordByEmail(String email, String encryptedPassword) {
        try {
            Connection conn = JDBCUtil.getConnection();
            String sql = "UPDATE users SET password = ? WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, encryptedPassword);
            stmt.setString(2, email);

            int rows = stmt.executeUpdate();

            JDBCUtil.closeStatement(stmt);
            JDBCUtil.closeConnection(conn);
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public String getEncryptedPasswordByEmail(String email) {
        String encryptedPassword = null;
        try {
            Connection conn = JDBCUtil.getConnection();
            String sql = "SELECT password FROM users WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                encryptedPassword = rs.getString("password");
            }

            JDBCUtil.closeResultSet(rs);
            JDBCUtil.closeStatement(stmt);
            JDBCUtil.closeConnection(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return encryptedPassword;
    }
}