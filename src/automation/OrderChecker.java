package automation;

import java.sql.*;
import java.util.concurrent.*;
import database.JDBCUtil;

public class OrderChecker {

    // NOTE: thời gian quy định phiên trễ (có thể sửa sau)
    private static final long MAX_SESSION_DURATION_HOURS = 2;

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

                if (hours >= MAX_SESSION_DURATION_HOURS && !OvertimeSessionManager.isOvertime(tableID)) {
                    // Gửi thông báo tới client (VD: nhân viên)
                    NotificationSender.sendNotification("192.168.88.134", "🕒 Bàn " + tableID + " đã order quá 2 tiếng!");

                    // Ghi nhớ bàn này để đổi màu dòng (nếu có UI)
                    OvertimeSessionManager.addOvertimeTable(tableID);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
