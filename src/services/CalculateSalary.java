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
            // Bắt đầu giao dịch
            conn.setAutoCommit(false);

            // Bước 1: Lấy danh sách tất cả staff và position
            String staffQuery = "SELECT staffID, position FROM STAFF WHERE staffID IS NOT NULL AND position IS NOT NULL";
            try (PreparedStatement staffStmt = conn.prepareStatement(staffQuery);
                 ResultSet staffRs = staffStmt.executeQuery()) {

                // Bước 2: Load dữ liệu hiệu suất từ RANKING_STAFF
                Map<String, RoleSalaryInfo> rankingMap = new HashMap<>();
                String rankingQuery = "SELECT staffID, totalDishes, rating, ranking FROM RANKING_STAFF WHERE staffID IS NOT NULL";
                try (Statement rankingStmt = conn.createStatement();
                     ResultSet rankingRs = rankingStmt.executeQuery(rankingQuery)) {
                    while (rankingRs.next()) {
                        String staffID = rankingRs.getString("staffID");
                        int totalDishes = rankingRs.getInt("totalDishes");
                        double rating = rankingRs.getDouble("rating");
                        int ranking = rankingRs.getInt("ranking");
                        rankingMap.put(staffID, new RoleSalaryInfo(totalDishes, rating, ranking));
                    }
                }

                // Bước 3: Chuẩn bị query
                String checkExist = "SELECT COUNT(*) FROM SALARY_RECORDS WHERE staffID = ? AND month = ?";
                String insertSalary = "INSERT INTO SALARY_RECORDS (staffID, month, baseSalary, bonus, total) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE baseSalary = VALUES(baseSalary), bonus = VALUES(bonus), total = VALUES(total), createdAt = CURRENT_TIMESTAMP";

                try (PreparedStatement checkStmt = conn.prepareStatement(checkExist);
                     PreparedStatement insertStmt = conn.prepareStatement(insertSalary)) {

                    while (staffRs.next()) {
                        String staffID = staffRs.getString("staffID");
                        String position = staffRs.getString("position");

                        // Bỏ qua nếu dữ liệu không hợp lệ
                        if (staffID == null || position == null || month == null) {
                            System.out.println("Skipped invalid record: staffID=" + staffID + ", month=" + month + ", position=" + position);
                            continue;
                        }

                        // Kiểm tra tồn tại
                        checkStmt.setString(1, staffID);
                        checkStmt.setString(2, month);
                        try (ResultSet checkRs = checkStmt.executeQuery()) {
                            boolean exists = checkRs.next() && checkRs.getInt(1) > 0;

                            double bonus = 0;
                            if ("nhân viên".equalsIgnoreCase(position) && rankingMap.containsKey(staffID)) {
                                RoleSalaryInfo info = rankingMap.get(staffID);
                                bonus = info.totalDishes * 2000 + info.rating * 50000;
                            } else {
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
                                }
                            }

                            double total = baseSalary + bonus;

                            // Sử dụng insert với ON DUPLICATE KEY UPDATE
                            insertStmt.setString(1, staffID);
                            insertStmt.setString(2, month);
                            insertStmt.setDouble(3, baseSalary);
                            insertStmt.setDouble(4, bonus);
                            insertStmt.setDouble(5, total);
                            int rowsAffected = insertStmt.executeUpdate();
                            if (rowsAffected > 0) {
                                System.out.println((exists ? "Updated" : "Inserted") + " salary for staffID=" + staffID + ", month=" + month + ", total=" + total);
                            }
                        }
                    }
                }

                // Commit giao dịch
                conn.commit();
                System.out.println("Tính lương hoàn tất cho tháng: " + month);
            }
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