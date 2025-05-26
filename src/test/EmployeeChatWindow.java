package test;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class EmployeeChatWindow {
    private JFrame chatFrame;
    private JTextArea txtChat;
    private BufferedReader in;
    private PrintWriter out;
    private String employeeName;

    public EmployeeChatWindow(String employeeName) {
        System.out.println("✅ Constructor EmployeeChatWindow được gọi!");
        this.employeeName = employeeName;
        initUI();
        connectToServer();  // Nếu dòng này không chạy, tức là đã bị dừng trước đó.
    }

    private void initUI() {
        chatFrame = new JFrame("Tin nhắn - " + employeeName);
        chatFrame.setSize(300, 400);
        chatFrame.setLayout(new BorderLayout());

        txtChat = new JTextArea(15, 25);
        txtChat.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtChat);

        chatFrame.add(scrollPane, BorderLayout.CENTER);
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.setVisible(true);
    }

    @SuppressWarnings({ "unused", "resource" })
	private void connectToServer() {
    	System.out.println("🔍 connectToServer() đã được gọi!");
        try {
            Socket socket = new Socket("localhost", 12349);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Gửi tên nhân viên đến Server
            out.println(employeeName);

            // Luồng lắng nghe tin nhắn từ Quản lý
            try {
                System.out.println("Đang kiểm tra kết nối Socket...");
                if (socket == null) {
                    throw new IOException("Socket chưa được khởi tạo!");
                }
                System.out.println("Socket OK, tiếp tục tạo luồng...");
                
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("BufferedReader OK, tiếp tục tạo luồng...");

                new Thread(() -> {
                    try {
                        String message;
                        while ((message = in.readLine()) != null) {
                            System.out.println("📥 Tin nhắn nhận được: " + message);
                            appendMessageToChat(message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Không thể kết nối đến Server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            chatFrame.dispose();
        }
    }

    // ✅ Phương thức cập nhật giao diện
    private void appendMessageToChat(String message) {
        SwingUtilities.invokeLater(() -> {
            if (txtChat == null) {
                System.out.println("⚠ txtChat chưa được khởi tạo!");
            } else {
                txtChat.append(message + "\n");
                System.out.println("📌 Đã cập nhật giao diện với tin nhắn: " + message);
            }
        });
    }

    public static void main(String[] args) {
    	String employeeName = JOptionPane.showInputDialog("Nhập tên nhân viên:");
    	System.out.println("📌 Tên nhân viên nhập vào: " + employeeName);
    	if (employeeName != null && !employeeName.trim().isEmpty()) {
    	    new EmployeeChatWindow(employeeName);
    	} else {
    	    System.out.println("⚠ Không có tên nhân viên, chương trình sẽ không chạy.");
    	}
    }
}
