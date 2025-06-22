package automation;

public class AdminBackgroundRunning {
    public static void main(String[] args) {
        OrderChecker.startMonitoring();
        System.out.println("Đang chạy kiểm tra order nền...");
    }
}