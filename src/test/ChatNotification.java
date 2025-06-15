package test;

import javax.swing.*;

import data_access_object.StaffDAO;
import views.ManagementSystem;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ChatNotification {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Quản lý nhà hàng");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout(FlowLayout.RIGHT));

        // Tạo JLabel có icon tin nhắn
        ImageIcon chatIcon = new ImageIcon((ManagementSystem.class.getResource("/icon/icons8-chat-30.png"))); // Thay bằng đường dẫn icon
        JLabel lblChat = new JLabel(chatIcon);
        lblChat.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Khi click vào lblChat, mở cửa sổ chat
        lblChat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openChatWindow();
            }
        });

        frame.add(lblChat);
        frame.setVisible(true);
    }

    // Hàm mở cửa sổ chat
//    public static void openChatWindow() {
//        JOptionPane.showMessageDialog(null, "Mở cửa sổ chat!");
//    }
    
    public static void openChatWindow() {
        JFrame chatFrame = new JFrame("Tin nhắn");
        chatFrame.setSize(300, 400);
        chatFrame.setLayout(new BorderLayout());

        List<String> employees = StaffDAO.getAllStaffNamesFromDatabase();
        employees.add(0, "Tất cả"); 

        JComboBox<String> cbEmployees = new JComboBox<>(employees.toArray(new String[0]));

        JPanel panelTop = new JPanel();
        panelTop.add(new JLabel("Gửi đến:"));
        panelTop.add(cbEmployees);

        JTextArea txtChat = new JTextArea(15, 25);
        txtChat.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtChat);

        JTextField txtInput = new JTextField(20);
        JButton btnSend = new JButton("Gửi");

        JPanel panelBottom = new JPanel();
        panelBottom.add(txtInput);
        panelBottom.add(btnSend);

        ChatClients client = new ChatClients("Quản lý");

        btnSend.addActionListener(e -> {
            String selectedEmployee = (String) cbEmployees.getSelectedItem();
            String message = txtInput.getText().trim();
            if (!message.isEmpty()) {
                if (selectedEmployee.equals("Tất cả")) {
                    client.sendMessage("Tất cả: [Thông báo chung] " + message);
                } else {
                    client.sendMessage(selectedEmployee + ": [Gửi đến " + selectedEmployee + "] " + message);
                }
                txtChat.append("Bạn: " + message + "\n");
                txtInput.setText("");
            }
        });

        chatFrame.add(panelTop, BorderLayout.NORTH);
        chatFrame.add(scrollPane, BorderLayout.CENTER);
        chatFrame.add(panelBottom, BorderLayout.SOUTH);
        chatFrame.setVisible(true);
    }
}
