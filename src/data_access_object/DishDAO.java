package data_access_object;

import java.awt.Image;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.Predicate;

import javax.swing.JOptionPane;

import database.JDBCUtil;
import models.Dish;
import models.Table;

public class DishDAO {
	
	private static DishDAO instance;
	
	public static DishDAO getInstance() {
		if(instance == null) {
			instance = new DishDAO();
		}
		return instance;
	}
	
	public static HashMap<String, Dish> map;
    public static ArrayList<Dish> list;
    public static ArrayList<Dish> originalList; // Lưu trạng thái ban đầu của danh sách

    public static void loadData() {
    	map = new HashMap<String, Dish>();
        list = new ArrayList<>();

        String sql = "SELECT * FROM dish";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Dish dish = new Dish(
                        rs.getString("dishID"),
                        rs.getString("dishName"),
                        rs.getDouble("dishPrice"),
                        rs.getString("dishCategory"),
                        rs.getString("dishImage")
                );
                map.put(dish.getDishID(), dish);
                list.add(dish);
                
            }
            CloneData();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void CloneData() {
        originalList = new ArrayList<>(list); // Tạo bản sao của danh sách
    }

    public static List<Dish> handleSort(boolean isChecked) {
    	if (isChecked) {
            System.out.println("Sắp xếp theo tên từ A -> Z");
            // Sắp xếp list theo DishID
            list.sort(Comparator.comparing(Dish::getDishName));
        } else {
            System.out.println("Khôi phục lại sắp xếp");
            // Khôi phục danh sách từ bản sao gốc
            list.clear();
            for (Dish dish : originalList) {
                list.add(dish);
            }
        }
        return list;
    }
    
    public static Dish accessDish(int index) {
        if (index < 0 || index >= list.size()) {
            System.out.println("Index món ăn không hợp lệ: " + index);
            return null;
        }
        return list.get(index);
    }

	public static boolean addDish(Dish dish) {
		
		if (getDish(dish.getDishID()) != null) {
			JOptionPane.showMessageDialog(null, "Thêm món thất bại!", "Thông báo", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		try (Connection connection = JDBCUtil.getConnection()) {
			addDishToDatabase(dish, connection);
			DishDAO.list.add(dish);
			DishDAO.map.put(dish.getDishID(), dish);
			DishDAO.originalList.add(dish);
		} catch (Exception e) {
			e.printStackTrace();
		}
        JOptionPane.showMessageDialog(null, "Đã món mới thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
		return true;
	}

	public static Dish getDish(String dishID) {
		Optional<Dish> result = list.stream().filter(new Predicate<Dish>() {
			@Override
			public boolean test(Dish dish) {
				return dish.getDishID().equals(dishID);
			}
		}).findFirst();

		return result.orElse(null);
	}

	public static boolean updateDish(Dish dish, Dish newDish) {
		String oldID = dish.getDishID();
		String newID = newDish.getDishID();

		// Nếu không tìm thấy món ăn cần cập nhật
		if (!map.containsKey(oldID)) {
			JOptionPane.showMessageDialog(null, "Không tìm thấy món ăn cần cập nhật!", "Thông báo", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		if (!oldID.equals(newID) && map.containsKey(newID)) {
			JOptionPane.showMessageDialog(null, "Cập nhật món ăn thất bại, ID đã tồn tại!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getDishID().equals(oldID)) {
				list.set(i, newDish);
			}
		}

		for (int i = 0; i < originalList.size(); i++) {
			if (originalList.get(i).getDishID().equals(oldID)) {
				originalList.set(i, newDish);
			}
		}
		
		try (Connection connection = JDBCUtil.getConnection()) {
			updateDishToDatabase(dish, newDish, connection);

			map.remove(oldID);
			map.put(newID, newDish);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		JOptionPane.showMessageDialog(null, "Cập nhật món ăn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
		return true;
	}

	public static boolean deleteDish(String id) {
		if (!DishDAO.map.containsKey(id)) return false;
		Dish dish1 = DishDAO.getDish(id);
		
		try (Connection connection = JDBCUtil.getConnection()) {

			// delete from map
			map.remove(id);

		    // Xóa món ăn khỏi list
		    list.removeIf(dish -> dish.getDishID().equals(id));
			originalList.removeIf(dish -> dish.getDishID().equals(id));


			deleteDishToDatabase(dish1, connection);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public static String getDishImage(String dishID) {
	    String imagePath = null;
	    Connection conn = JDBCUtil.getConnection();
	    String query = "SELECT dishImage FROM dish WHERE dishID = ?";
	    try (PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.setString(1, dishID);
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            imagePath = rs.getString("dishImage"); // Lấy đường dẫn hình ảnh từ CSDL
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return imagePath;
	}

	public static void storeData() {
		try (Connection conn = JDBCUtil.getConnection()) {

			clearTable(conn);
			insertData(conn);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void clearTable(Connection conn) {
		String delete_sql = "DELETE FROM dish";
		
		try (PreparedStatement stmt = conn.prepareStatement(delete_sql)) {
			stmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void insertData(Connection conn) {
		String sql = "INSERT INTO dish (dishID, dishName, dishPrice, dishCategory, dishImage) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			for (Dish dish : list) {
				stmt.setString(1, dish.getDishID());
				stmt.setString(2, dish.getDishName());
				stmt.setDouble(3, dish.getDishPrice());
				stmt.setString(4, dish.getDishCategory());
				stmt.setString(5, dish.getDishImage());
				
				stmt.executeUpdate();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateImageToData(String dishID, String imagePath) {
		String sqlString = "UPDATE dish SET dishImage = ? WHERE dishID = ?";
		
		try (Connection connection = JDBCUtil.getConnection(); 
				PreparedStatement stmtPreparedStatement = connection.prepareStatement(sqlString)) {
			stmtPreparedStatement.setString(1, imagePath);
			stmtPreparedStatement.setString(2, dishID);
			
			stmtPreparedStatement.executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static int countRows() {
		int rowCount = 0;
		try {
            // Kết nối cơ sở dữ liệu
            Connection conn = JDBCUtil.getConnection();

            // Câu lệnh SQL
            String query = "SELECT COUNT(*) AS total_rows FROM dish";

            // Thực thi câu lệnh
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Đọc kết quả
            if (rs.next()) {
                rowCount = rs.getInt("total_rows");
            }

            // Đóng kết nối
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return rowCount;
	}
	

	public static void addDishToDatabase(Dish dish, Connection conn) {
		String sql = "INSERT INTO dish VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, dish.getDishID());
			stmt.setString(2, dish.getDishName());
			stmt.setDouble(3, dish.getDishPrice());
			stmt.setString(4, dish.getDishCategory());
			stmt.setString(5, dish.getDishImage());
			
			stmt.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void deleteDishToDatabase(Dish dish, Connection conn) {
		String delete_sql = "DELETE FROM dish WHERE dishID = ?";
		
		try (PreparedStatement stmt = conn.prepareStatement(delete_sql)) {
			stmt.setString(1, dish.getDishID());
			stmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void updateDishToDatabase(Dish dish, Dish newDish, Connection conn) {
		deleteDishToDatabase(dish, conn);
		addDishToDatabase(newDish, conn);
	}
}
