package controllers;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import views.Login;

public class OperatingSystemController {
	
	public static void exit(JLabel exit) {
	    exit.addMouseListener(new MouseAdapter() {
	        @Override
	        public void mouseClicked(MouseEvent e) {
	            // Hiển thị hộp thoại xác nhận
	            int result = JOptionPane.showConfirmDialog(
	                null, 
	                "Mày thoát chương trình hả?", 
	                "Xác nhận thoát", 
	                JOptionPane.OK_CANCEL_OPTION, 
	                JOptionPane.WARNING_MESSAGE
	            );

	            // Nếu người dùng nhấn "OK"
	            if (result == JOptionPane.OK_OPTION) {
	                // Đóng JFrame hiện tại
	                JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(exit);
	                currentFrame.dispose();

	                // Hiển thị cửa sổ Login
	                Login login = new Login();
	                login.setUndecorated(true);
	                login.setVisible(true);
	            }
	        }
	    });
	}

}
