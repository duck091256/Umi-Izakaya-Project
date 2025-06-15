package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCUtil {
	
	public static Connection getConnection() {
		
		String url = "jdbc:mysql://localhost:3306/umi_izakaya";
		String userName = "root";
		String password = "";
		
		Connection con = null;
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection(url, userName, password);
			System.out.println("Connect successfully!");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return con;
    }
	
	public static Connection getConnectionNoDB() {
	    Connection conn = null;
	    try {
	        Class.forName("com.mysql.cj.jdbc.Driver");
	        String url = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC";
	        String user = "root";
	        String password = ""; // sửa theo máy của bạn
	        conn = DriverManager.getConnection(url, user, password);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return conn;
	}

    public static void closeConnection(Connection connection) {
    	try {
    		if(connection != null) {
    			connection.close();
    		}
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
	// Phương thức đóng PreparedStatement
	public static void closeStatement(PreparedStatement st) {
		try {
			if (st != null) {
				st.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Phương thức đóng ResultSet nếu có sử dụng
	public static void closeResultSet(ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
