package util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    private static final String DB_severName = "26.177.116.29";
    private static final String DB_login = "sa";
    private static final String DB_password = "123456";
    private static final String DB_databaseName = "restaurantManagementSystem";

    public static Connection getConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = "jdbc:sqlserver://" + DB_severName + ":1433;" + "databaseName=" + DB_databaseName
                    + "; encrypt = true; trustServerCertificate = true";

            return DriverManager.getConnection(url, DB_login, DB_password);
        }catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void closeConnection(Connection con) {
        try {
            if (con != null) {
                con.close();
                System.out.println("closed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
