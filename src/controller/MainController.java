package controller;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import data_access_object.*;
import view.ManagementSystem;
import view.OrderingSystem;

public class MainController {
	
	public void lbl_Close_CloseFunction(JLabel lbl_Close) {
		lbl_Close.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }
        });
	}
	
	public void lbl_Close_MouseListener (JLabel lbl_Close) {
		lbl_Close.addMouseListener(new MouseAdapter() {
			@Override
            public void mouseEntered(MouseEvent e) {
            	lbl_Close.setBackground(new Color(232, 17, 35));
            	lbl_Close.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
            	lbl_Close.setBackground(new Color(240, 240, 240));
            	lbl_Close.setForeground(Color.BLACK);
            }
		});
	}
	
	public void lbl_Login_MouseListener(JLabel lbl_Login ,JTextField txfUsername, JPasswordField passwordField) {
        lbl_Login.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                lbl_Login.setBackground(new Color(71, 74, 76));
                lbl_Login.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {

                lbl_Login.setBackground(new Color(30, 31, 32));
                lbl_Login.setForeground(Color.WHITE);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
            	
            	if (txfUsername == null || passwordField == null) {
                    System.err.println("Error: Text fields are not initialized.");
                    return;
                }
            	
                LoginDAO next = new LoginDAO();

                String username = txfUsername.getText();
                String password = new String(passwordField.getPassword());

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        null,
                        "Username and password must not be empty!",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                
                String position = next.getUserRole(username, password);

                if (position != null) {
                    JOptionPane.showMessageDialog(null, "Đăng nhập thành công!");

                    JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(lbl_Login);
                    currentFrame.dispose();

                    if (position.equalsIgnoreCase("Quản lí")) {
                        ManagementSystem managementSystem = new ManagementSystem();
                        managementSystem.setAccountInformation(username);
                        managementSystem.setUndecorated(true);
                        managementSystem.setVisible(true);
                    } else if (position.equalsIgnoreCase("Nhân viên")) {
                        OrderingSystem orderingSystem = new OrderingSystem();
                        orderingSystem.setAccountInformation(username); // nếu có
                        orderingSystem.setUndecorated(true);
                        orderingSystem.setVisible(true);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Đăng nhập thất bại!");
                }
            }
        });
    }
	
    public void onUsernameFocusGained(JTextField txfUsername) {
        if (txfUsername.getText().equals("Username")) {
        	txfUsername.setText("");
        	txfUsername.setBackground(new Color(223, 223, 223));
        	txfUsername.setForeground(Color.black);
        }
    }

    public void onUsernameFocusLost(JTextField txfUsername) {
        if (txfUsername.getText().equals("")) {
        	txfUsername.setText("Username");
        	txfUsername.setOpaque(true);
        	txfUsername.setForeground(new Color(223, 223, 223));
        	txfUsername.setBackground(Color.WHITE);
        }
    }

    public void onPasswordFocusGained(JPasswordField passwordField) {
        if (String.valueOf(passwordField.getPassword()).equals("Password")) {
            passwordField.setText("");
            passwordField.setBackground(new Color(223, 223, 223));
            passwordField.setForeground(Color.BLACK);
        }
    }

    public void onPasswordFocusLost(JPasswordField passwordField) {
        if (String.valueOf(passwordField.getPassword()).equals("")) {
            passwordField.setText("Password");
            passwordField.setOpaque(true);
            passwordField.setForeground(new Color(223, 223, 223));
            passwordField.setBackground(Color.WHITE);
        }
    }
}
