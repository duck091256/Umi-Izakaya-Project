package controllers;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import data_access_object.LoginDAO;
import models.User;
import utils.AESUtil;
import views.FoodOrderingSystem;
import views.LoginPage;
import views.ManagementSystem;
import views.OrderingSystem;
import javafx.application.Platform;

public class MainController {

    private static boolean fxInitialized = false;

    public static void initFX() {
        if (!fxInitialized) {
            Platform.startup(() -> {}); // chỉ gọi đúng 1 lần
            fxInitialized = true;
        }
    }

    public static void runLater(Runnable task) {
        if (!fxInitialized) {
            initFX(); // khởi động nếu chưa có
        }
        Platform.runLater(task);
    }
    
	// Validate Email and Password
	public boolean isValidEmail(String email) {
		return email.endsWith("@gmail.com");
	}

	public boolean isValidPassword(String password) {
		return password.length() >= 6 && password.matches(".*[A-Z].*") && // ít nhất một chữ in hoa
				password.matches(".*[!@#$%^&*().].*") && // ít nhất một ký tự đặc biệt
				password.matches(".*[a-zA-Z0-9].*"); // có ký tự thường hoặc số
	}

	public void lbl_Close_CloseFunction(JLabel lbl_Close) {
		lbl_Close.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.exit(0);
			}
		});
	}

	public void lbl_Close_MouseListener(JLabel lbl_Close) {
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

	public void lbl_Login_MouseListener(JLabel lbl_Login, JTextField txfUsername, JPasswordField passwordField) {
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

				String username = txfUsername.getText();
				String password = new String(passwordField.getPassword());

				if (username.isEmpty() || password.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Username and password must not be empty!", "Input Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				LoginDAO dao = new LoginDAO();
				User user = dao.getUserByUsername(username);

				if (user == null) {
				    JOptionPane.showMessageDialog(null, "User not found!", "Login Error", JOptionPane.ERROR_MESSAGE);
				    return;
				}

				String position = user.getPosition();

				try {
				    String decryptedPassword = AESUtil.decrypt(user.getEncryptedPassword());
				    if (decryptedPassword.equals(password)) {
				        JOptionPane.showMessageDialog(null, "Login Success!");

				        JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(lbl_Login);
				        currentFrame.dispose();

				        if (position.equalsIgnoreCase("Quản lí")) {
				            ManagementSystem managementSystem = new ManagementSystem();
				            managementSystem.setAccountInformation(username);
				            managementSystem.setUndecorated(true);
				            managementSystem.setVisible(true);
				        } else if (position.equalsIgnoreCase("Nhân viên")) {
				        	runLater(() -> {
				        	    FoodOrderingSystem fxSystem = new FoodOrderingSystem(username);
				        	    fxSystem.show();
				        	});			        	
				        } else {
				            JOptionPane.showMessageDialog(null, "Unknown position!", "Login Error", JOptionPane.ERROR_MESSAGE);
				        }

				    } else {
				        // Trường hợp password sai
				        JOptionPane.showMessageDialog(null, "Password was wrong!", "Login Error", JOptionPane.ERROR_MESSAGE);
				    }
				} catch (Exception ex) {
				    ex.printStackTrace();
				    JOptionPane.showMessageDialog(null, "Error decrypting password!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}
	
	public void lbl_Register_MouseListener(JLabel lbl_Register, JTextField txfUsername, JTextField emailField, JPasswordField passwordField) {
	    lbl_Register.addMouseListener(new MouseAdapter() {
	        @Override
	        public void mouseEntered(MouseEvent e) {
	            lbl_Register.setBackground(new Color(71, 74, 76));
	        }

	        @Override
	        public void mouseExited(MouseEvent e) {
	            lbl_Register.setBackground(new Color(30, 31, 32));
	        }

	        @Override
	        public void mouseClicked(MouseEvent e) {
	            String username = txfUsername.getText();
	            String email = emailField.getText();
	            String password = new String(passwordField.getPassword());

	            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
	                JOptionPane.showMessageDialog(null, "Please fill in all fields.");
	                return;
	            }

	            if (!email.endsWith("@gmail.com")) {
	                JOptionPane.showMessageDialog(null, "Email must end with @gmail.com");
	                return;
	            }

	            if (!isValidPassword(password)) {
	                JOptionPane.showMessageDialog(null, "Password must be at least 6 characters long, contain uppercase letters, special characters, and letters/numbers.");
	                return;
	            }

	            try {
	                String encryptedPassword = AESUtil.encrypt(password);
	                LoginDAO dao = new LoginDAO();

	                boolean registered = dao.registerUser(username, email, encryptedPassword);

	                if (registered) {
	                    JOptionPane.showMessageDialog(null, "Registration successful!");
	                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(lbl_Register);
	                    frame.dispose();

	                    LoginPage login = new LoginPage();
	                    login.setUndecorated(true); // Ẩn thanh tiêu đề
	                    login.setVisible(true);
	                } else {
	                    JOptionPane.showMessageDialog(null, "Registration failed. Username or email might already exist.");
	                }
	            } catch (Exception ex) {
	                ex.printStackTrace();
	                JOptionPane.showMessageDialog(null, "Encryption error or database failure.");
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

    public void onEmailFocusGained(JTextField EmailField) {
        if (EmailField.getText().equals("Email")) {
        	EmailField.setText("");
        	EmailField.setBackground(new Color(223, 223, 223));
        	EmailField.setForeground(Color.black);
        }
    }

    public void onEmailFocusLost(JTextField EmailField) {
        if (EmailField.getText().equals("")) {
        	EmailField.setText("Email");
        	EmailField.setOpaque(true);
        	EmailField.setForeground(new Color(223, 223, 223));
        	EmailField.setBackground(Color.WHITE);
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
