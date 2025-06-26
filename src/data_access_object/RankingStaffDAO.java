package data_access_object;

import database.JDBCUtil;
import models.RankingStaff;
import models.SimpleRanking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RankingStaffDAO {
    private static RankingStaffDAO instance;

    public static RankingStaffDAO getInstance() {
        if (instance == null) {
            instance = new RankingStaffDAO();
        }
        return instance;
    }

    /**
     * Thêm hoặc cập nhật thông tin ranking của staff
     *
     * @param staffID        ID của nhân viên
     * @param totalSessions  Tổng số phiên phục vụ
     * @param totalDishes    Tổng số món đã phục vụ
     * @param rating		 Điểm xét hạng của nhân viên
     * @param ranking        Xếp hạng của nhân viên
     * @return true nếu thành công, false nếu thất bại
     */
    public static boolean updateRankings(String staffID, int totalSessions, int totalDishes, double rating, int ranking) {
        String sql = "INSERT INTO ranking_staff (staffID, totalSessions, totalDishes, rating, ranking) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE totalSessions = ?, totalDishes = ?, rating = ?, ranking = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, staffID);
            ps.setInt(2, totalSessions);
            ps.setInt(3, totalDishes);
            ps.setDouble(4, rating);
            ps.setInt(5, ranking);
            ps.setInt(6, totalSessions);
            ps.setInt(7, totalDishes);
            ps.setDouble(8, rating);
            ps.setInt(9, ranking);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lấy thông tin ranking của staff theo ID
     *
     * @param staffID ID của nhân viên
     * @return RankingStaff hoặc null nếu không tìm thấy
     */
    public static RankingStaff getRankingStaffByID(String staffID) {
        String sql = "SELECT * FROM ranking_staff WHERE staffID = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, staffID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new RankingStaff(
                    rs.getString("staffID"),
                    rs.getInt("totalSessions"),
                    rs.getInt("totalDishes"),
                    rs.getDouble("rating"),
                    rs.getInt("ranking")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy tất cả thông tin ranking của staff
     *
     * @return ArrayList<RankingStaff> danh sách tất cả staff
     */
    public static ArrayList<RankingStaff> getAllRankingStaff() {
        ArrayList<RankingStaff> list = new ArrayList<>();
        String sql = "SELECT * FROM ranking_staff";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new RankingStaff(
                    rs.getString("staffID"),
                    rs.getInt("totalSessions"),
                    rs.getInt("totalDishes"),
                    rs.getDouble("rating"),
                    rs.getInt("ranking")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public static List<RankingStaff> getRankingList() {
        List<RankingStaff> list = new ArrayList<>();
        String sql = "SELECT * FROM ranking_staff ORDER BY ranking ASC";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
            	RankingStaff sr = new RankingStaff(
                    rs.getString("staffID"),
                    rs.getInt("totalSessions"),
                    rs.getInt("totalDishes"),
                    rs.getDouble("rating"),
                    rs.getInt("ranking")
                );
                list.add(sr);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    
    public static List<SimpleRanking> getSimpleRankingList() {
        List<SimpleRanking> list = new ArrayList<>();
        String sql = "SELECT staffID, rating, ranking FROM ranking_staff ORDER BY ranking ASC";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String staffID = rs.getString("staffID");
                String fullName = StaffDAO.getStaffNameFromDatabaseByID(staffID);
                double rating = rs.getDouble("rating");
                int ranking = rs.getInt("ranking");

                list.add(new SimpleRanking(ranking, fullName, rating));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}