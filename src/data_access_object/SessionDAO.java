package data_access_object;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import database.JDBCUtil;
import models.Session;

public class SessionDAO {
    private static SessionDAO instance;

    public static SessionDAO getInstance() {
        if (instance == null) {
            instance = new SessionDAO();
        }
        return instance;
    }

    // Thêm session mới
    public void insertSession(Connection conn, Session session) throws SQLException {
        String sql = "INSERT INTO sessions (staffID, tableID, billID) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, session.getStaffID());
            stmt.setString(2, session.getTableID());
            stmt.setString(3, session.getBillID());
            stmt.executeUpdate();
        }
    }

    // Cập nhật session (ví dụ thay đổi bill hoặc table)
    public boolean updateSession(Connection conn, Session session) {
        String sql = "UPDATE sessions SET staffID = ?, tableID = ?, billID = ? WHERE sessionID = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, session.getStaffID());
            ps.setString(2, session.getTableID());
            ps.setString(3, session.getBillID());
            ps.setInt(4, session.getSessionID());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Xóa session theo tableID
    public boolean deleteSession(Connection conn, String tableID) {
        String sql = "DELETE FROM sessions WHERE tableID = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tableID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Lấy tất cả session
    public static ArrayList<Session> getAllSessions() {
        ArrayList<Session> list = new ArrayList<>();
        String sql = "SELECT * FROM sessions";

        try (Connection conn = JDBCUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int sessionID = rs.getInt("sessionID");
                String staffID = rs.getString("staffID");
                String tableID = rs.getString("tableID");
                String billID = rs.getString("billID");

                Session s = new Session(sessionID, staffID, tableID, billID);
                list.add(s);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm session theo ID
    public Session getSessionByID(int sessionID) {
        String sql = "SELECT * FROM sessions WHERE sessionID = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Session(
                    rs.getInt("sessionID"),
                    rs.getString("staffID"),
                    rs.getString("tableID"),
                    rs.getString("billID")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    // Tìm session theo table ID
    public static Session getSessionByTableID(String tableID) {
        String sql = "SELECT * FROM sessions WHERE tableID = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tableID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Session(
                    rs.getInt("sessionID"),
                    rs.getString("staffID"),
                    rs.getString("tableID"),
                    rs.getString("billID")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    public static int createSession(String staffID, String tableID, int billID) {
        int sessionID = -1;
        try (Connection conn = JDBCUtil.getConnection()) {
            String sql = "INSERT INTO Session (staffID, tableID, billID) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, staffID);
            stmt.setString(2, tableID);
            stmt.setInt(3, billID);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) sessionID = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sessionID;
    }
    
    public static LocalDateTime getStartTimeByTable(String tableID) {
        LocalDateTime result = null;
        String query = "SELECT b.time FROM sessions s JOIN bill b ON s.billID = b.billID WHERE s.tableID = ?";

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, tableID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String timeString = rs.getString("time");
                // Định dạng chuẩn khớp với DB
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                result = LocalDateTime.parse(timeString, formatter);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}