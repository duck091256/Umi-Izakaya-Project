package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import database.JDBCUtil;

public class CalculateSalary {
	public static void calculateAndSaveSalaryForAllStaff(YearMonth targetMonth) {
	    double baseSalary = 5_000_000;
	    String month = targetMonth.toString();

	    try (Connection conn = JDBCUtil.getConnection()) {
	        // Bước 1: Lấy danh sách tất cả staff và position
	        String staffQuery = "SELECT staffID, position FROM STAFF";
	        PreparedStatement staffStmt = conn.prepareStatement(staffQuery);
	        ResultSet staffRs = staffStmt.executeQuery();

	        // Bước 2: Load dữ liệu hiệu suất từ RANKING_STAFF
	        Map<String, RoleSalaryInfo> rankingMap = new HashMap<>();
	        String rankingQuery = "SELECT staffID, totalDishes, rating, ranking FROM RANKING_STAFF";
	        Statement rankingStmt = conn.createStatement();
	        ResultSet rankingRs = rankingStmt.executeQuery(rankingQuery);
	        while (rankingRs.next()) {
	            String staffID = rankingRs.getString("staffID");
	            int totalDishes = rankingRs.getInt("totalDishes");
	            double rating = rankingRs.getDouble("rating");
	            int ranking = rankingRs.getInt("ranking");
	            rankingMap.put(staffID, new RoleSalaryInfo(totalDishes, rating, ranking));
	        }

	        // Bước 3: Chuẩn bị query kiểm tra lương đã tồn tại chưa
	        String checkExist = "SELECT 1 FROM SALARY_RECORDS WHERE staffID = ? AND month = ?";
	        PreparedStatement checkStmt = conn.prepareStatement(checkExist);

	        // Bước 4: Chuẩn bị query chèn lương mới
	        String insertSalary = "INSERT INTO SALARY_RECORDS (staffID, month, baseSalary, bonus, total) VALUES (?, ?, ?, ?, ?)";

	        while (staffRs.next()) {
	            String staffID = staffRs.getString("staffID");
	            String position = staffRs.getString("position");

	            // Check nếu đã có bản ghi lương trong SALARY_RECORDS thì bỏ qua
	            checkStmt.setString(1, staffID);
	            checkStmt.setString(2, month);
	            ResultSet checkRs = checkStmt.executeQuery();
	            if (checkRs.next()) {
	                continue; // Đã có -> bỏ qua
	            }

	            // Tính lương nếu chưa có
	            double bonus = 0;

	            if ("nhân viên".equalsIgnoreCase(position) && rankingMap.containsKey(staffID)) {
	                // Phục vụ: Tính thưởng theo hiệu suất
	                RoleSalaryInfo info = rankingMap.get(staffID);
	                bonus = info.totalDishes * 2000 + info.rating * 50000;
	            } else {
	                // Các vai trò khác: thưởng cố định
	                switch (position.toLowerCase()) {
	                    case "quản lí":
	                    	baseSalary += 5_000_000;
	                        bonus = 2_000_000;
	                        break;
	                    case "pha chế":
	                        bonus = 2_500_000;
	                        break;
	                    case "thu ngân":
	                        bonus = 1_000_000;
	                        break;
	                    default:
	                }
	            }

	            // Sau khi tính toán xong
	            double total = baseSalary + bonus;

	            PreparedStatement insertStmt = conn.prepareStatement(insertSalary);
	            insertStmt.setString(1, staffID);
	            insertStmt.setString(2, month);
	            insertStmt.setDouble(3, baseSalary);
	            insertStmt.setDouble(4, bonus);
	            insertStmt.setDouble(5, total);
	            insertStmt.executeUpdate();

	        }

	        System.out.println("Tính lương hoàn tất cho tháng: " + month);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	static class RoleSalaryInfo {
	    int totalDishes;
	    double rating;
	    int ranking;

	    public RoleSalaryInfo(int totalDishes, double rating, int ranking) {
	        this.totalDishes = totalDishes;
	        this.rating = rating;
	        this.ranking = ranking;
	    }
	}
}
