package data_access_object;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import database.JDBCUtil;
import models.DetailReceipt;

public class DetailReceiptDAO {
	public static ArrayList<DetailReceipt> list = new ArrayList<>();
    private static Connection conn = JDBCUtil.getConnection();

    public static void loadData() {
        list.clear(); // Xóa dữ liệu cũ trước khi tải mới
        String query = "SELECT * FROM detail_receipt";
        
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String dishID = rs.getString("dishID");
                String billID = rs.getString("billID");
                int dishQuantity = rs.getInt("dishQuantity");

                DetailReceipt detail = new DetailReceipt(dishID, billID, dishQuantity);
                list.add(detail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
	public static List<DetailReceipt> getDetailReceiptList(String billID) {
		
        List<DetailReceipt> list = new ArrayList<>();
        String query = "SELECT dishID, dishQuantity FROM detail_receipt WHERE billID = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, billID);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                String dishID = rs.getString("dishID");
                int quantity = rs.getInt("dishQuantity");
                list.add(new DetailReceipt(dishID, billID, quantity));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
	

    /**
     * Lưu hoặc cập nhật danh sách chi tiết hóa đơn vào database
     *
     * @param conn Kết nối database
     * @param detailReceipts Danh sách chi tiết hóa đơn
     * @return true nếu thành công, false nếu thất bại
     */
	public static boolean storeOrUpdateDetailReceipt(Connection conn, ArrayList<DetailReceipt> detailReceipts) {
	    String upsertSql = "INSERT INTO detail_receipt (dishID, billID, dishQuantity) " +
	                      "VALUES (?, ?, ?) " +
	                      "ON DUPLICATE KEY UPDATE dishQuantity = VALUES(dishQuantity);";
	    try {
	        try (PreparedStatement upsertStmt = conn.prepareStatement(upsertSql)) {
	            for (DetailReceipt detail : detailReceipts) {
	                upsertStmt.setString(1, detail.getDishID());
	                upsertStmt.setString(2, detail.getBillID());
	                upsertStmt.setInt(3, detail.getDishQuantity());
	                upsertStmt.addBatch();
	            }
	            upsertStmt.executeBatch();
	        }
	        return true;
	    } catch (SQLException e) {
	        System.out.println("Lỗi khi lưu detail_receipt: " + e.getMessage());
	        e.printStackTrace();
	        return false;
	    }
	}
	
	public static void addDishToBill(int billID, String dishID, int quantity) {
	    try (Connection conn = JDBCUtil.getConnection()) {
	        String sql = "INSERT INTO DETAIL_RECEIPT (dishID, billID, dishQuantity) VALUES (?, ?, ?)";
	        PreparedStatement stmt = conn.prepareStatement(sql);
	        stmt.setString(1, dishID);
	        stmt.setInt(2, billID);
	        stmt.setInt(3, quantity);
	        stmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
}
