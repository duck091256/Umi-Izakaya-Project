package automation;

import java.sql.*;
import java.util.concurrent.*;
import database.JDBCUtil;

public class OrderChecker {
	private static String ipQuoatQuoat="172.20.10.4";
	private static String ipNHON="192.168.88.134";

    // NOTE: thời gian quy định phiên trễ (có thể sửa sau)
    private static final long MAX_SESSION_DURATION_HOURS = 3;
    
    public static long getDuration() {
    	return MAX_SESSION_DURATION_HOURS;
    }

    public static void startMonitoring() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> checkOvertimeSessions(), 0, 5, TimeUnit.MINUTES);
    }

    private static void checkOvertimeSessions() {
        try (Connection conn = JDBCUtil.getConnection()) {
            String sql = """
                SELECT s.tableID, b.time
                FROM Sessions s
                JOIN BILL b ON s.billID = b.billID
                WHERE b.wasPay = 0
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String tableID = rs.getString("tableID");
                Timestamp orderTime = rs.getTimestamp("time");

                long duration = System.currentTimeMillis() - orderTime.getTime();
                long hours = duration / (1000 * 60 * 60);

                if (hours >= MAX_SESSION_DURATION_HOURS) {
                    // Gửi thông báo tới client (VD: nhân viên)
                    NotificationSender.sendNotification(ipQuoatQuoat, "🕒 Bàn " + tableID + " đã order quá " + MAX_SESSION_DURATION_HOURS + " tiếng!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
