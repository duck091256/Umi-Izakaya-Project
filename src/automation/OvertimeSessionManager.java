package automation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import data_access_object.*;

public class OvertimeSessionManager {
	private static long MAX_DURATION_MINUTES = OrderChecker.getDuration() * 60;
	
    private static final Set<String> overtimeTables = ConcurrentHashMap.newKeySet();

    public static void addOvertimeTable(String tableID) {
        overtimeTables.add(tableID);
    }

    public static boolean isOvertime(String tableID) {
        LocalDateTime startTime = SessionDAO.getStartTimeByTable(tableID);
        if (startTime == null) return false;

        long minutes = Duration.between(startTime, LocalDateTime.now()).toMinutes();
        return minutes >= MAX_DURATION_MINUTES;
    }
    
    public static void removeOvertimeTable(String tableID) {
        overtimeTables.remove(tableID);
    }
}