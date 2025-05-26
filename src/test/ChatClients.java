package test;

import java.io.*;
import java.net.*;

public class ChatClients {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 12349;
    private BufferedReader in;
    private PrintWriter out;

    public ChatClients(String staffName) {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Gửi tên nhân viên đến Server
            out.println(staffName);

            // Luồng nhận tin nhắn
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
		if (out != null) {
			System.out.println("📤 Đang gửi tin nhắn: " + message); // ✅ Debug quan trọng
			out.println(message);
			out.flush(); // ✅ Đảm bảo gửi ngay lập tức
		} else {
			System.out.println("❌ Không thể gửi tin nhắn, out=null!");
		}
    }
}