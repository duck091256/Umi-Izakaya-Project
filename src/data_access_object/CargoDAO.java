package data_access_object;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

import database.JDBCUtil;
import model.Cargo;

public class CargoDAO {
	
	private static CargoDAO instance;
	
	public static CargoDAO getInstance() {
		if(instance == null) {
			instance = new CargoDAO();
		}
		return instance;
	}
	public static HashMap<String, Cargo> map;
	public static ArrayList<Cargo> list;

	/**
	 * Tải tất dữ liệu của nhân viên từ database vào list và map của chương trình
	 * 
	 * Sử dụng JDBC để thực hiện các câu lệnh truy vấn, close database sau khi hoàn tất.
	 *
	 */
	public static void loadData() {
		map = new HashMap<>();
		list = new ArrayList<>();
		
		String sql = "SELECT * FROM cargo";
		
		try (Connection conn = JDBCUtil.getConnection(); 	// Đổi lại hệ quản trị sau nhé
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				Cargo cargo = new Cargo(
						rs.getString("cargo_id"),
                        rs.getString("cargo_name"),
                        rs.getString("stock_quantity"),
                        rs.getString("price"),
                        rs.getDate("supplier"),
                        rs.getDate("expiration_date")
				);
				map.put(cargo.getCargo_id(), cargo);
				System.out.println("hello");
				list.add(cargo);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Thêm hàng hóa vào HashMap và List hiện tại của chương trình
	 * 
	 * Hàm sẽ kiểm tra PRIMARY KEY đã tồn tại hay chưa?
	 * 
	 * @param cargo nhận từ input người dùng nhập
	 * @return true nếu thành công, false nếu thất bại
	 */
	public static boolean addCargo(Cargo cargo) {
		
		if (CargoDAO.map.containsKey(cargo.getCargo_id())) {
	        JOptionPane.showMessageDialog(null, "Thêm hàng hóa thất bại!", "Thông báo", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		CargoDAO.list.add(cargo);
		CargoDAO.map.put(cargo.getCargo_id(), cargo);
        JOptionPane.showMessageDialog(null, "Đã thêm hàng hóa thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
		return true;
	}

	/**
	 * Tìm kiếm và trả về hàng hóa từ HashMap và List hiện tại của chương trình
	 * 
	 * Hàm sẽ kiểm tra PRIMARY KEY đã tồn tại hay chưa?
	 * 
	 * @param cargo_id - Nhận vào ID của hàng hóa để tìm kiếm
	 * @return trả về Cargo được sử dụng để load lại vào giao diện hoặc null
	 */
	public static Cargo getCargo(String cargo_id) {
		return CargoDAO.map.getOrDefault(cargo_id, null);
	}
	
	/**
	 * Cập nhật hàng hóa đã có trong HashMap và list hiện tại của chương trình
	 * 
	 * Hàm sẽ kiểm tra PRIMARY KEY đã tồn tại hay chưa?
	 * Cần truyền vào cargo trước khi update, là cargo được click vào để update
	 * 
	 * @param cargo là cargo được chọn để update, các thông tin nhận từ input người dùng nhập
	 * @return true nếu thành công, false nếu thất bại
	 */
	public static boolean updateCargo(Cargo cargo, Cargo newCargo) {
		
		/* Nếu không cập nhật ID của Staff **/
		if (newCargo.getCargo_id().equals(cargo.getCargo_id())) {
			CargoDAO.map.put(cargo.getCargo_id(), newCargo);
			JOptionPane.showMessageDialog(null, "Cập nhật hàng hóa thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
			return true;
		}
		/* Nếu cập nhật ID của Staff */
		// Nếu ID mới đã tồn tại
		if (CargoDAO.map.containsKey(newCargo.getCargo_id())) {
			JOptionPane.showMessageDialog(null, "Cập nhật hàng hóa thất bại XXX", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}

		// Nếu ID mới chưa tồn tại
		CargoDAO.map.remove(cargo.getCargo_id());
		CargoDAO.map.put(newCargo.getCargo_id(), newCargo);
		return true;
	}

	/**
	 * Cập nhật nhân viên đã có trong HashMap và list hiện tại của chương trình
	 *
	 * Hàm sẽ kiểm tra PRIMARY KEY đã tồn tại hay chưa?
	 * Cần truyền vào staff trước khi update, là staff được click vào để update
	 *
	 * @param id là staff được chọn để update, các thông tin nhận từ input người dùng nhập
	 * @return true nếu thành công, false nếu thất bại
	 */
	public static boolean deleteCargo(String id) {
		if (!CargoDAO.map.containsKey(id)) {
			JOptionPane.showMessageDialog(null, "Xóa hàng hóa thất bại XXX", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}

		Cargo cargo = CargoDAO.map.get(id);
		CargoDAO.map.remove(id);
		list.remove(cargo);
		JOptionPane.showMessageDialog(null, "Xóa hàng hóa thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
		return true;
	}
	
	/**
	 * Lưu trữ tất dữ liệu của nhân viên vào database
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
		String delete_sql = "DELETE FROM cargo";
		
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
		String sql = "INSERT INTO cargo VALUES (?, ?, ?, ?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			for (Cargo cargo : map.values()) {
				stmt.setString(1, cargo.getCargo_id());
				stmt.setString(2, cargo.getCargo_name());
				stmt.setString(3, cargo.getStock_quantity());
				stmt.setString(4, cargo.getPrice());
				stmt.setDate(5, cargo.getSuppiler());
				stmt.setDate(6, cargo.getExpiration_date());
				
				stmt.executeUpdate();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
