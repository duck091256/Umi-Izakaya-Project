package test;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12349;
    private static HashMap<String, PrintWriter> clientWriters = new HashMap<>();

    private static boolean isServerRunning = false; // Theo dõi trạng thái server

    public static void startChatServerIfNeeded() {
        if (!isServerRunning) {
            new Thread(() -> {
                try {
                    isServerRunning = true;
                    ChatServer.main(new String[]{}); // Gọi server
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 Server chat đang chạy...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String employeeName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Nhận tên nhân viên
                employeeName = in.readLine();
                System.out.println("📢 Nhân viên kết nối: " + employeeName);
                // Gửi tin nhắn chào mừng ngay khi nhân viên kết nối
                out.println("📢 Chào mừng " + employeeName + " đến hệ thống chat!");
                out.flush();
                System.out.println("✅ Đã gửi tin nhắn chào mừng đến " + employeeName);
                synchronized (clientWriters) {
                    clientWriters.put(employeeName, out);
                }

                // Lắng nghe tin nhắn từ quản lý
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("📩 Tin nhắn nhận được từ quản lí: " + message);
                    sendMessageToCorrectRecipient(message);
                }
            } catch (IOException e) {
                System.out.println("⚠ Nhân viên " + employeeName + " đã ngắt kết nối.");
            } finally {
                synchronized (clientWriters) {
                    clientWriters.remove(employeeName);
                }
            }
        }

        private void sendMessageToCorrectRecipient(String message) {
            System.out.println("📢 Gửi tin nhắn chung: " + message); // ✅ Debug

            String[] parts = message.split(":", 2);
            if (parts.length < 2) {
            	System.out.println("❌ Tin nhắn không đúng định dạng! [" + message + "]"); // ✅ Debug
                return;
            }

            String recipient = parts[0].trim();
            String text = parts[1].trim();

            synchronized (clientWriters) {
            	System.out.println("📋 Danh sách nhân viên online: " + clientWriters.keySet());
            	
                if (recipient.equals("Tất cả")) {
                    for (PrintWriter writer : clientWriters.values()) {
                        writer.println("📢 [Quản lý] " + text);
                        writer.flush();
                    }
                } else if (clientWriters.containsKey(recipient)) {
                    System.out.println("📩 Gửi tin nhắn đến: " + recipient); // ✅ Debug
                    clientWriters.get(recipient).println("📩 [Quản lý] " + text);
                    clientWriters.get(recipient).flush();
                } else {
                    System.out.println("❌ Không tìm thấy nhân viên: " + recipient); // ✅ Debug
                }
            }
        }
    }
}