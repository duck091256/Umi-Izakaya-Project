package database;

import java.sql.Connection;
public class Test {
	public static void main(String[] args) {
		Connection connection = JDBCUtil.getConnection();
		
		if(connection != null) {
			System.out.println("Kết nối thành công!");
		} else {
			System.out.println("Kết nối thất bại!");
		}
	}
}
