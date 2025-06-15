package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.YearMonth;

import database.JDBCUtil;

public class CalculateSalary {
    public static double calculateBonus(int totalDishes, double rating, int ranking) {
        double bonus = 0;
        if (totalDishes > 100) bonus += 500_000;
        if (rating >= 4.5) bonus += 300_000;
        if (ranking <= 3) bonus += 200_000;
        return bonus;
    }
    
    public static void calculateAndSaveSalaryForAllStaff(YearMonth targetMonth) {
        double baseSalary = 5_000_000;
        String month = targetMonth.toString();
        
        try (Connection conn = JDBCUtil.getConnection()) {
            String checkExist = "SELECT * FROM SALARY_RECORDS WHERE staffID = ? AND month = ?";
            String rankingQuery = "SELECT staffID, totalDishes, rating, ranking FROM RANKING_STAFF";
            PreparedStatement rankStmt = conn.prepareStatement(rankingQuery);
            ResultSet rs = rankStmt.executeQuery();

            String insertSalary = "INSERT INTO SALARY_RECORDS (staffID, month, baseSalary, bonus, total) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSalary);
            PreparedStatement checkStmt = conn.prepareStatement(checkExist);

            while (rs.next()) {
                String staffID = rs.getString("staffID");
                int dishes = rs.getInt("totalDishes");
                double rating = rs.getDouble("rating");
                int ranking = rs.getInt("ranking");

                // Check nếu đã có lương rồi thì bỏ qua
                checkStmt.setString(1, staffID);
                checkStmt.setString(2, month);
                ResultSet checkRs = checkStmt.executeQuery();
                if (checkRs.next()) continue;

                double bonus = calculateBonus(dishes, rating, ranking);
                double total = baseSalary + bonus;

                insertStmt.setString(1, staffID);
                insertStmt.setString(2, month);
                insertStmt.setDouble(3, baseSalary);
                insertStmt.setDouble(4, bonus);
                insertStmt.setDouble(5, total);
                insertStmt.addBatch();
            }

            insertStmt.executeBatch();
            System.out.println("Đã lưu lương tháng " + month);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
