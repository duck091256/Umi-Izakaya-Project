package automation;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class NotificationSender {
    public static void sendNotification(String clientIP, String message) {
        try (Socket socket = new Socket(clientIP, 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(message);
        } catch (IOException e) {
            System.out.println("Không gửi được thông báo đến " + clientIP);
        }
    }
}