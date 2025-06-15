package views;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import controllers.MainController;

import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JPasswordField;
import java.awt.Color;
import java.awt.Font;

public class LoginPage extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private Point mouseClickPoint; // Lưu tọa độ khi nhấn chuột
    private JTextField txfUsername;
    private JPasswordField passwordField;
    private JLabel lbl_Login;
    private JLabel lbl_Close;
    private MainController controller;
    private JLabel PasswordText;
    private JLabel UsernameText;
    private JLabel lblDontHaveAcc;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    LoginPage frame = new LoginPage();
                    frame.setUndecorated(true); // Ẩn thanh tiêu đề
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public LoginPage() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 700, 394);
        setLocationRelativeTo(null);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        controller = new MainController();
        
        ImageIcon icon = new ImageIcon(getClass().getResource("/image/Login_Interface.png"));
        Image image = icon.getImage();
        Image scaledImage = image.getScaledInstance(700, 394, Image.SCALE_SMOOTH);
        contentPane.setLayout(null);
        
        lbl_Close = new JLabel("X");
        lbl_Close.setFont(new Font("YouYuan", Font.PLAIN, 11));
        lbl_Close.setBounds(627, 11, 49, 14);
        lbl_Close.setOpaque(true);
        lbl_Close.setBackground(new Color(240, 240, 240));
        lbl_Close.setForeground(new Color(0, 0, 0));
        lbl_Close.setHorizontalAlignment(SwingConstants.CENTER);
        lbl_Close.setVerticalAlignment(SwingConstants.CENTER);
        
        controller.lbl_Close_CloseFunction(lbl_Close);
        controller.lbl_Close_MouseListener(lbl_Close);
        
        contentPane.add(lbl_Close);
        
        passwordField = new JPasswordField();
        passwordField.setBounds(227, 150, 252, 29);
        passwordField.setBackground(new Color(223, 223, 223));
        passwordField.setForeground(Color.BLACK);
        contentPane.add(passwordField);

        PasswordText = new JLabel("Password");
        PasswordText.setBounds(227, 150, 252, 29);
        PasswordText.setForeground(Color.GRAY);
        contentPane.add(PasswordText);

        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                controller.onPasswordFocusGained(passwordField);
            }

            @Override
            public void focusLost(FocusEvent e) {
                controller.onPasswordFocusLost(passwordField);
            }
        });
        
        txfUsername = new JTextField();
        txfUsername.setBounds(227, 111, 252, 29);
        txfUsername.setBackground(new Color(223, 223, 223));
        txfUsername.setForeground(Color.BLACK);
        txfUsername.setColumns(10);
        contentPane.add(txfUsername);
        
        UsernameText = new JLabel("Username");
        UsernameText.setBounds(227, 111, 252, 29);
        UsernameText.setOpaque(true);
        UsernameText.setForeground(Color.GRAY);
        contentPane.add(UsernameText);
        
        txfUsername.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                controller.onUsernameFocusGained(txfUsername);
            }

            public void focusLost(FocusEvent e) {
                controller.onUsernameFocusLost(txfUsername);
            }
        });

        lbl_Login = new JLabel("Login");
        lbl_Login.setOpaque(true);
        lbl_Login.setBackground(new Color(30, 31, 32));
        lbl_Login.setForeground(new Color(255, 255, 255));
        lbl_Login.setBounds(227, 210, 252, 25);
        lbl_Login.setHorizontalAlignment(SwingConstants.CENTER);
        lbl_Login.setVerticalAlignment(SwingConstants.CENTER);

        controller.lbl_Login_MouseListener(lbl_Login, txfUsername, passwordField);
        
        contentPane.add(lbl_Login);
        
        JLabel lblForgotPassword = new JLabel("Forgot Password?");
        lblForgotPassword.setFont(new Font("Tahoma", Font.BOLD, 9));
        lblForgotPassword.setForeground(new Color(255, 255, 255));
        lblForgotPassword.setBounds(227, 185, 100, 14);
        contentPane.add(lblForgotPassword);
        
        lblForgotPassword.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ForgotPasswordDialog.showDialog(LoginPage.this);
			}
		});
        
        lblDontHaveAcc = new JLabel("Don't have an account?");
        lblDontHaveAcc.setFont(new Font("Tahoma", Font.BOLD, 9));
        lblDontHaveAcc.setForeground(Color.WHITE);
        lblDontHaveAcc.setBounds(363, 185, 116, 14);
        contentPane.add(lblDontHaveAcc);
        
        lblDontHaveAcc.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				dispose();

                RegisterPage registerPage = new RegisterPage();
                registerPage.setUndecorated(true); // Ẩn thanh tiêu đề
                registerPage.setVisible(true);
				
			}
		});

        JLabel label = new JLabel(new ImageIcon(scaledImage));
        label.setBounds(0, 0, 700, 394);
        contentPane.add(label);

        setContentPane(contentPane);

        // Chức năng di chuyển cửa sổ
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseClickPoint = e.getPoint();
                setOpacity(0.6f);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setOpacity(1.0f);
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point currentPoint = e.getLocationOnScreen();
                setLocation(currentPoint.x - mouseClickPoint.x, currentPoint.y - mouseClickPoint.y);
            }
        });
    }
}