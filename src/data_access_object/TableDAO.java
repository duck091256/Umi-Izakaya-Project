package data_access_object;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.JOptionPane;

import database.JDBCUtil;
import models.Dish;
import models.Staff;
import models.Table;
import services.Ordering;

public class TableDAO {
	
	private static TableDAO instance;
	
	public static TableDAO getInstance() {
		if(instance == null) {
			instance = new TableDAO();
		}
		return instance;
	}
	
	public static ArrayList<Table> list;

    /**
     * Tải dữ liệu bàn từ cơ sở dữ liệu và tái tạo orderList, tableBillMap
     */
    public static void loadData() {
        list = new ArrayList<>();
        
        // Tải danh sách bàn
        String tableSql = "SELECT tableID, floorStay, operatingStatus, responsibleBy FROM dining_table";
        
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(tableSql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Table table = new Table(
                    rs.getString("tableID"),
                    rs.getString("floorStay"),
                    rs.getString("operatingStatus"),
                    rs.getString("responsibleBy")
                );
                list.add(table);
            }
            
            // Tải sessions và detail_receipt với truy vấn tối ưu
            String orderSql = "SELECT s.tableID, s.billID, dr.dishID, dr.dishQuantity, d.dishName, d.dishPrice, d.dishCategory, d.dishImage " +
                            "FROM sessions s " +
                            "JOIN bill b ON s.billID = b.billID " +
                            "LEFT JOIN detail_receipt dr ON b.billID = dr.billID " +
                            "LEFT JOIN dish d ON dr.dishID = d.dishID " +
                            "WHERE b.wasPay = 0";
            
            try (PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {
                ResultSet orderRs = orderStmt.executeQuery();
                
                HashMap<String, ArrayList<Dish>> tempOrderList = new HashMap<>();
                HashMap<String, String> tempTableBillMap = new HashMap<>();
                
                while (orderRs.next()) {
                    String tableID = orderRs.getString("tableID");
                    String billID = orderRs.getString("billID");
                    String dishID = orderRs.getString("dishID");
                    
                    if (dishID != null) { // Chỉ xử lý nếu có món
                        int dishQuantity = orderRs.getInt("dishQuantity");
                        String dishName = orderRs.getString("dishName");
                        double dishPrice = orderRs.getDouble("dishPrice");
                        String dishCategory = orderRs.getString("dishCategory");
                        String dishImage = orderRs.getString("dishImage");
                        
//                        System.out.println("TableID: " + tableID + " - DishID: " + dishID + " - Quantity: " + dishQuantity);
                        Dish dish = new Dish(dishID, dishName, dishPrice, dishCategory, dishImage, dishQuantity);
                        
                        ArrayList<Dish> dishes = tempOrderList.computeIfAbsent(tableID, k -> new ArrayList<>());
                        dishes.add(dish);
                    }
                    
                    tempTableBillMap.put(tableID, billID);
                }
                
                // Cập nhật orderList và tableBillMap
                synchronized (Ordering.class) {
                    Ordering.orderList.clear();
                    Ordering.orderList.putAll(tempOrderList);
                    Ordering.tableBillMap.clear();
                    Ordering.tableBillMap.putAll(tempTableBillMap);
                }
            }
            
        } catch (Exception e) {
            System.out.println("Lỗi khi tải dữ liệu bàn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tìm kiếm và trả về table từ tableID do người dùng nhập
     *
     * Hàm sẽ kiểm tra PRIMARY KEY đã tồn tại hay chưa?
     * 
     * @param tableID - Nhận vào ID của bàn để tìm kiếm
     * @return trả về Table được sử dụng để load lại vào giao diện hoặc null
     */
    public static Table getTable(String tableID) {
        if (tableID == null || tableID.trim().isEmpty()) {
            return null;
        }
        
        if (list.isEmpty()) {
            loadData();
        }
        
        return list.stream()
                   .filter(table -> table.getTableID().equals(tableID))
                   .findFirst()
                   .orElse(null);
    }

    public static String getStaffResponsible(String tableID) {
        return Optional.ofNullable(getTable(tableID))
                       .map(Table::getResponsibleBy)
                       .orElse("");
    }
    
    /**
     * Kiểm tra xem bàn có session đang hoạt động không
     */
    public static boolean hasActiveSession(String tableID) {
        String sql = "SELECT COUNT(*) FROM sessions s JOIN bill b ON s.billID = b.billID WHERE s.tableID = ? AND b.wasPay = 0";
        
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tableID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (Exception e) {
            System.out.println("Lỗi khi kiểm tra session: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
	/**
	 * Thêm table vào list
	 *
	 * Hàm sẽ kiểm tra PRIMARY KEY đã tồn tại hay chưa?
	 * 
	 * @param table nhận từ input người dùng nhập
	 * @return true nếu thành công, false nếu thất bại
	 */
	public static boolean addTable(Table table) {

		if (getTable(table.getTableID()) != null) {
			JOptionPane.showMessageDialog(null, "Thêm bàn thất bại!", "Thông báo", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		TableDAO.list.add(table);
		addTableToDatabase(table, JDBCUtil.getConnection());
        JOptionPane.showMessageDialog(null, "Đã thêm bàn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
		return true;
	}

	/**
	 * Cập nhật thông tin bàn trong danh sách.
	 *
	 * @param oldTable Bàn cũ (bàn cần cập nhật thông tin).
	 * @param newTable Bàn mới (bàn với thông tin cần cập nhật).
	 * @return true - nếu cập nhật thành công, false - nếu thất bại.
	 */
	public static boolean updateTable(Table oldTable, Table newTable) {
	    String id = oldTable.getTableID(); // Giữ nguyên ID, không cho đổi

	    // Tìm bàn cũ trong danh sách
	    Table existingTable = null;
	    for (Table table : list) {
	        if (table.getTableID().equals(id)) {
	            existingTable = table;
	            break;
	        }
	    }

	    // Nếu không tìm thấy bàn cũ
	    if (existingTable == null) {
	        JOptionPane.showMessageDialog(null, "Không tìm thấy bàn cần cập nhật!", "Thông báo", JOptionPane.WARNING_MESSAGE);
	        return false;
	    }

	    // Cập nhật dữ liệu trong database (chỉ update dữ liệu khác, không đổi tableID)
	    updateTableToDatabase(existingTable, newTable, JDBCUtil.getConnection());

	    // Cập nhật trong list (vẫn giữ nguyên tableID cũ)
	    existingTable.setFloorStay(newTable.getFloorStay());
	    existingTable.setOperatingStatus(newTable.getOperatingStatus());
	    existingTable.setResponsibleBy(newTable.getResponsibleBy());
	    existingTable.setAvailable(newTable.getAvailable());

	    JOptionPane.showMessageDialog(null, "Cập nhật bàn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
	    return true;
	}
	
	/**
	 * Thay đổi người phụ trách của bàn
	 * 
	 * Hàm kiểm tra người phụ trách mới có tồn tại không?
	 *
	 * @param tableID ID của bàn cần thay đổi
	 * @param staffID ID của người phụ trách mới
	 * @return trả về true nếu thành công, false nếu thất bại
	 */
	public static boolean updateResponsible(String tableID, String staffID) {
		Table table = getTable(tableID);
		Staff staff = StaffDAO.getStaff(staffID);
		

		if (table == null || staff == null) return false;

		table.setResponsibleBy(staffID);
		updateResponsibleToData(tableID, staffID);
		return true;
	}
	
	public static void updateResponsibleToData(String tableID, String staffID) {
		String sqlString = "UPDATE dining_table SET responsibleBy = ? WHERE tableID = ?";
		
		try (Connection connection = JDBCUtil.getConnection(); 
				PreparedStatement stmtPreparedStatement = connection.prepareStatement(sqlString)) {
			stmtPreparedStatement.setString(1, staffID);
			stmtPreparedStatement.setString(2, tableID);
			
			stmtPreparedStatement.executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Xóa table dựa vào tableID
	 *
	 * Hàm sẽ kiểm tra tableID có tồn tại không và thực hiện xóa bàn
	 *
	 * @param tableID id của bàn cần xóa
	 * @return true nếu thành công, false nếu thất bại
	 */
	public static boolean deleteTable(String tableID) {
		try (Connection connection = JDBCUtil.getConnection()) {
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getTableID().equals(tableID)) {
					deleteTableToDatabase(list.get(i), connection);
					list.remove(i);
					return true;
				}
			}
			return false;	
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Lưu trữ tất dữ liệu của table vào database
	 * 
	 * Sử dụng JDBC để thực hiện các câu lệnh truy vấn, close database sau khi hoàn tất.
	 *
	 */
	public static void storeData() {
		try (Connection conn = JDBCUtil.getConnection()) {

			clearTable(conn);
			insertData(conn);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Xóa tất cả các dữ liệu cũ khỏi database
	 * 
	 * Sử dụng JDBC để thực hiện các câu lệnh truy vấn, close database sau khi hoàn tất.
	 * 
	 * @param conn - Connection đã được kết nối với database
	 */
	private static void clearTable(Connection conn) {
		String delete_sql = "DELETE FROM dining_table";
		
		try (PreparedStatement stmt = conn.prepareStatement(delete_sql)) {
			stmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Thêm dữ liệu từ list vào database
	 *  
	 * Sử dụng JDBC để thực hiện các câu lệnh truy vấn, close database sau khi hoàn tất.
	 * 
	 * @param conn - Connection đã được kết nối với database
	 */
	private static void insertData(Connection conn) {
		String sql = "INSERT INTO dining_table VALUES (?, ?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			for (Table table : list) {
				stmt.setString(1, table.getTableID());
				stmt.setString(2, table.getFloorStay());
				stmt.setString(3, table.getOperatingStatus());
				stmt.setString(4, table.getResponsibleBy());
				
				stmt.executeUpdate();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    public static Table accessTable(int index) {
    	Table table = list.get(index);
    	return table;
    }
	
	public static Map<Integer, Integer> numOfTableByFloor() {
	    Map<Integer, Integer> tableCountByFloor = new HashMap<>();
	    try {
	        // Kết nối cơ sở dữ liệu
	        Connection conn = JDBCUtil.getConnection();

	        // Câu lệnh SQL với GROUP BY
	        String query = "SELECT floorStay, COUNT(*) AS number_of_table FROM dining_table GROUP BY floorStay";

	        // Thực thi câu lệnh
	        Statement stmt = conn.createStatement();
	        ResultSet rs = stmt.executeQuery(query);

	        // Đọc kết quả
	        while (rs.next()) {
	            int floorStay = rs.getInt("floorStay");
	            int numberOfTable = rs.getInt("number_of_table");
	            tableCountByFloor.put(floorStay, numberOfTable);
	        }

	        // Đóng kết nối
	        conn.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return tableCountByFloor;
	}
	

	public static void addTableToDatabase(Table table, Connection conn) {
		String sql = "INSERT INTO dining_table VALUES (?, ?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, table.getTableID());
			stmt.setString(2, table.getFloorStay());
			stmt.setString(3, table.getOperatingStatus());
			stmt.setString(4, table.getResponsibleBy());
			
			stmt.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public static void deleteTableToDatabase(Table table, Connection conn) {
		String delete_sql = "DELETE FROM dining_table WHERE tableID = ?";
		
		try (PreparedStatement stmt = conn.prepareStatement(delete_sql)) {
			stmt.setString(1, table.getTableID());
			stmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateTableToDatabase(Table table, Table newTable, Connection conn) {
	    String sql = "UPDATE dining_table SET floorStay = ?, operatingStatus = ?, responsibleBy = ? WHERE tableID = ?";

	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setString(1, newTable.getFloorStay());
	        ps.setString(2, newTable.getOperatingStatus());
	        ps.setString(3, newTable.getResponsibleBy());
	        ps.setString(4, table.getTableID());

	        ps.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
}
