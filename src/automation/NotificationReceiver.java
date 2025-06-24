package automation;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class NotificationReceiver {
    private static TrayIcon trayIcon;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Đang lắng nghe thông báo...");

            // Khởi tạo SystemTray 1 lần
            if (SystemTray.isSupported()) {
                setupSystemTray();
            } else {
                System.out.println("⚠️ Máy không hỗ trợ SystemTray.");
            }

            while (true) {
                Socket socket = serverSocket.accept();

                new Thread(() -> {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String msg = in.readLine();
                        System.out.println("📩 Đã nhận: " + msg);
                        showNotification(msg);
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Thiết lập TrayIcon một lần duy nhất
    private static void setupSystemTray() {
        try {
            SystemTray tray = SystemTray.getSystemTray();

            // Tạo icon 16x16 trắng trống
            Image image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

            trayIcon = new TrayIcon(image, "Hệ thống thông báo");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hiện thông báo trên góc phải
    private static void showNotification(String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage("🔔 Thông báo từ hệ thống", message, TrayIcon.MessageType.INFO);
        } else {
            // Nếu lỗi, fallback sang popup
            fallbackDialog(message);
        }
    }

    private static void fallbackDialog(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });
    }
}