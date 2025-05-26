package util;

import java.sql.Connection;

public class TestDB {

	public static void main(String[] args) {
		try {
			Connection connection = DBConnection.getConnection();
			if (connection == null) {
				System.out.print("K dc");
			} else {
				System.out.println("ok");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
