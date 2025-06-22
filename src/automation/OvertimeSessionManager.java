package automation;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OvertimeSessionManager {
    private static final Set<String> overtimeTables = ConcurrentHashMap.newKeySet();

    public static void addOvertimeTable(String tableID) {
        overtimeTables.add(tableID);
    }

    public static boolean isOvertime(String tableID) {
        return overtimeTables.contains(tableID);
    }
    
    public static void removeOvertimeTable(String tableID) {
        overtimeTables.remove(tableID);
    }
}