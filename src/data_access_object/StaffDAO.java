package data_access_object;

import java.sql.*;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

import javax.swing.JOptionPane;
import database.JDBCUtil;
import models.*;
import utils.*;

public class StaffDAO {
	
	private static StaffDAO instance;
	
	public static StaffDAO getInstance() {
		if(instance == null) {
			instance = new StaffDAO();
		}
		return instance;
	}
	public static HashMap<String, Staff> map;
	public static ArrayList<Staff> list;

	/**
	 * Tải tất dữ liệu của nhân viên từ database vào list và map của chương trình
	 * 
	 * Sử dụng JDBC để thực hiện các câu lệnh truy vấn, close database sau khi hoàn tất.
	 *
	 */
	public static void loadData() {
		map = new HashMap<>();
		list = new ArrayList<>();
		
		String sql = "SELECT * FROM staff";
		
		try (Connection conn = JDBCUtil.getConnection(); 
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				Staff staff = new Staff(
						rs.getString("staffID"),
                        rs.getString("userName"),
                        rs.getString("password"),
                        rs.getString("fullName"),
                        rs.getString("sex"),
                        rs.getString("phone"),
                        rs.getString("position"),
                        rs.getTime("startShift"),
                        rs.getTime("endShift")
				);
				map.put(staff.getStaffID(), staff);
				list.add(staff);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Thêm nhân viên vào HashMap và List hiện tại của chương trình
	 * 
	 * Hàm sẽ kiểm tra PRIMARY KEY đã tồn tại hay chưa?
	 * 
	 * @param staff nhận từ input người dùng nhập
	 * @return true nếu thành công, false nếu thất bại
	 */
	public static boolean addStaff(Staff staff) {
	    if (StaffDAO.map.containsKey(staff.getStaffID())) {
	        JOptionPane.showMessageDialog(null, "Thêm nhân viên thất bại! Mã đã tồn tại!", "Thông báo", JOptionPane.ERROR_MESSAGE);
	        return false;
	    }

	    Connection connection = null;
	    try {
	        connection = JDBCUtil.getConnection();
	        connection.setAutoCommit(false); // Bắt đầu transaction

	        // 1. Thêm nhân viên vào bảng STAFF
	        addStaffToDatabase(staff, connection);

	        // 2. Thêm bản ghi lương vào SALARY_RECORDS với lương mặc định 0.0 và tháng hiện tại
	        String sql = "INSERT INTO salary_records (staffID, month, baseSalary) VALUES (?, ?, ?)";
	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setString(1, staff.getStaffID());
	            ps.setString(2, YearMonth.now().toString()); // Sử dụng tháng hiện tại (ví dụ: 2025-06)
	            ps.setDouble(3, 0.0); // Lương mặc định
	            ps.executeUpdate();
	        }

	        connection.commit(); // Commit nếu cả 2 thành công

	        // Cập nhật danh sách và hiển thị thông báo
	        StaffDAO.list.add(staff);
	        StaffDAO.map.put(staff.getStaffID(), staff);
	        JOptionPane.showMessageDialog(null, "Đã thêm nhân viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
	        return true;

	    } catch (Exception e) {
	        e.printStackTrace();
	        try {
	            if (connection != null) connection.rollback(); // Rollback nếu lỗi
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	        JOptionPane.showMessageDialog(null, "Thêm nhân viên thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
	        return false;
	    } finally {
	        try {
	            if (connection != null) connection.setAutoCommit(true);
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	}

	/**
	 * Tìm kiếm và trả về nhân viên từ HashMap và List hiện tại của chương trình
	 * 
	 * Hàm sẽ kiểm tra PRIMARY KEY đã tồn tại hay chưa?
	 * 
	 * @param staffID - Nhận vào ID của nhân viên để tìm kiếm
	 * @return trả về Staff được sử dụng để load lại vào giao diện hoặc null
	 */
	public static Staff getStaff(String staffID) {
		return StaffDAO.map.getOrDefault(staffID, null);
	}
	
	/**
	 * Cập nhật nhân viên đã có trong HashMap và list hiện tại của chương trình
	 * 
	 * Hàm sẽ kiểm tra PRIMARY KEY đã tồn tại hay chưa?
	 * Cần truyền vào staff trước khi update, là staff được click vào để update
	 * 
	 * @param staff là staff được chọn để update, các thông tin nhận từ input người dùng nhập
	 * @return true nếu thành công, false nếu thất bại
	 */
	public static boolean updateStaff(Staff staff, Staff newStaff) {
		/* Nếu không cập nhật ID của Staff **/
		if (newStaff.getStaffID().equals(staff.getStaffID())) {
			staff.updateInfo(newStaff);
			updateStaffToDatabase(staff, newStaff, JDBCUtil.getConnection());
			JOptionPane.showMessageDialog(null, "Cập nhật nhân viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
			return true;
		}
		/* Nếu cập nhật ID của Staff */
		// Nếu ID mới đã tồn tại
		if (StaffDAO.map.containsKey(newStaff.getStaffID())) {
			JOptionPane.showMessageDialog(null, "Cập nhật nhân viên thất bại XXX", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}

		// Nếu ID mới chưa tồn tại
		updateStaffToDatabase(staff, newStaff, JDBCUtil.getConnection());
		
		StaffDAO.map.remove(staff.getStaffID());
		staff.updateInfo(newStaff);
		StaffDAO.map.put(staff.getStaffID(), staff);
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
	public static boolean deleteStaff(String id) {
		if (!StaffDAO.map.containsKey(id)) {
			JOptionPane.showMessageDialog(null, "Xóa nhân viên thất bại XXX", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}

		try (Connection connection = JDBCUtil.getConnection()) {
			Staff staff = StaffDAO.map.get(id);
			deleteStaffToDatabase(staff, connection);
			StaffDAO.map.remove(id);
			list.remove(staff);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		JOptionPane.showMessageDialog(null, "Xóa nhân viên thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
		return true;
	}
	
	public static void addStaffToDatabase(Staff staff, Connection conn) {
		String sql = "INSERT INTO staff VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, staff.getStaffID());
			stmt.setString(2, staff.getUserName());
			stmt.setString(3, staff.getPassword());
			stmt.setString(4, staff.getFullName());
			stmt.setString(5, staff.getSex());
			stmt.setString(6, staff.getPhone());
			stmt.setString(7, staff.getPosition());
			stmt.setTime(8, staff.getStartShift());
			stmt.setTime(9,  staff.getEndShift());
			
			stmt.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteStaffToDatabase(Staff staff, Connection conn) {
		String delete_sql = "DELETE FROM staff WHERE staffID = ?";
		
		try (PreparedStatement stmt = conn.prepareStatement(delete_sql)) {
			stmt.setString(1, staff.getStaffID());
			stmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		String delete_sql = "DELETE FROM staff";
		
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
		String sql = "INSERT INTO staff VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			for (Staff staff : list) {
				stmt.setString(1, staff.getStaffID());
				stmt.setString(2, staff.getUserName());
				stmt.setString(3, staff.getPassword());
				stmt.setString(4, staff.getFullName());
				stmt.setString(5, staff.getSex());
				stmt.setString(6, staff.getPhone());
				stmt.setString(7, staff.getPosition());
				stmt.setTime(8, staff.getStartShift());
				stmt.setTime(9,  staff.getEndShift());
				
				stmt.executeUpdate();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Lấy danh sách tên của các nhân viên từ danh sách nhân viên.
	 *
	 * @return ArrayList<String> Danh sách tên nhân viên
	 */
	public static ArrayList<String> getStaffNames() {
	    ArrayList<String> staffNames = new ArrayList<>();

	    if (list != null) {
	        for (Staff staff : list) {
	            if (staff != null && staff.getFullName() != null) {
	                staffNames.add(staff.getFullName());
	            }
	        }
	    }

	    return staffNames;
	}
	
	public static void updateStaffToDatabase(Staff staff, Staff newStaff, Connection conn) {
	    String sql = "UPDATE staff SET fullName = ?, sex = ?, phone = ?, position = ? WHERE staffID = ?";

	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setString(1, newStaff.getFullName());
	        ps.setString(2, newStaff.getSex());
	        ps.setString(3, newStaff.getPhone());
	        ps.setString(4, newStaff.getPosition());
	        ps.setString(5, staff.getStaffID()); // giữ nguyên staffID

	        ps.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	public static String getStaffNameFromDatabaseByID(String staffID) {
        String sql = "SELECT * FROM staff WHERE staffID = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, staffID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("fullName");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "";
    }
	
	public static List<String> getAllStaffNamesFromDatabase() {
	    List<String> names = new ArrayList<>();
	    String sql = "SELECT fullName FROM staff";

	    try (Connection conn = JDBCUtil.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql);
	         ResultSet rs = ps.executeQuery()) {

	        while (rs.next()) {
	            names.add(rs.getString("fullName"));
	        }

	    } catch (SQLException e) {
	        System.err.println("Lỗi khi lấy tên nhân viên: " + e.getMessage());
	    }

	    return names;
	}
	
	// Thêm phương thức để lấy staffID dựa trên username
    public static String getStaffIDByUsername(String username) {
        String sql = "SELECT staffID FROM staff WHERE username = ?";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
        	ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getString("staffID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu không tìm thấy
    }    
    
    public static double getBaseSalaryData(String staffID) {
        try (Connection conn = JDBCUtil.getConnection()) {
            String sql = "SELECT baseSalary FROM salary_records WHERE staffID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            
            ps.setString(1, staffID);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("baseSalary");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return 0.0;
    }
    
    public static boolean insertBaseSalaryData(String staffID, double baseSalary) {
        String sql = "INSERT INTO salary_records (staffID, baseSalary) VALUES (?, ?)";
        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, staffID);
            ps.setDouble(2, baseSalary);

            int rowsInserted = ps.executeUpdate();
            return rowsInserted > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateBaseSalaryData(String staffID, double newSalary) {
        String sql = "UPDATE salary_records SET baseSalary = ? WHERE staffID = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, newSalary);
            ps.setString(2, staffID);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static boolean updateTotalSalaryData(String staffID, double total) {
        String sql = "UPDATE salary_records SET total = ? WHERE staffID = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, total);
            ps.setString(2, staffID);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
