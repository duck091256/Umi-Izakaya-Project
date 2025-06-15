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

public class RegisterPage extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private Point mouseClickPoint; // Lưu tọa độ khi nhấn chuột
    private JTextField txfUsername;
    private JPasswordField passwordField;
    private JLabel lbl_Register;
    private JLabel lbl_Close;
    private MainController controller;
    private JLabel PasswordText;
    private JLabel UsernameText;
    private JLabel lblAlreadyHaveAcc;
    private JTextField EmailField;
    private JLabel EmailText;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    RegisterPage frame = new RegisterPage();
                    frame.setUndecorated(true); // Ẩn thanh tiêu đề
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public RegisterPage() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 700, 394);
        setLocationRelativeTo(null);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        controller = new MainController();
        
        ImageIcon icon = new ImageIcon(getClass().getResource("/image/Registry_Interface.png"));
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
        passwordField.setBounds(227, 186, 252, 29);
        passwordField.setBackground(new Color(223, 223, 223));
        passwordField.setForeground(Color.BLACK);
        contentPane.add(passwordField);

        PasswordText = new JLabel("Password");
        PasswordText.setBounds(227, 186, 252, 29);
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
        txfUsername.setBounds(227, 118, 252, 29);
        txfUsername.setBackground(new Color(223, 223, 223));
        txfUsername.setForeground(Color.BLACK);
        txfUsername.setColumns(10);
        contentPane.add(txfUsername);
        
        UsernameText = new JLabel("Username");
        UsernameText.setBounds(227, 118, 252, 29);
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
        
        EmailField = new JTextField();
        EmailField.setForeground(Color.BLACK);
        EmailField.setColumns(10);
        EmailField.setBackground(new Color(223, 223, 223));
        EmailField.setBounds(227, 152, 252, 29);
        contentPane.add(EmailField);
        
        EmailText = new JLabel("Email");
        EmailText.setOpaque(true);
        EmailText.setForeground(Color.GRAY);
        EmailText.setBounds(227, 152, 252, 29);
        contentPane.add(EmailText);
        
        EmailField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                controller.onEmailFocusGained(EmailField);
            }

            public void focusLost(FocusEvent e) {
                controller.onEmailFocusLost(EmailField);
            }
        });

        lbl_Register = new JLabel("Register");
        lbl_Register.setOpaque(true);
        lbl_Register.setBackground(new Color(30, 31, 32));
        lbl_Register.setForeground(new Color(255, 255, 255));
        lbl_Register.setBounds(227, 230, 252, 25);
        lbl_Register.setHorizontalAlignment(SwingConstants.CENTER);
        lbl_Register.setVerticalAlignment(SwingConstants.CENTER);

        controller.lbl_Register_MouseListener(lbl_Register, txfUsername, EmailField, passwordField);
        
        contentPane.add(lbl_Register);
        
        lblAlreadyHaveAcc = new JLabel("I already have an account!");
        lblAlreadyHaveAcc.setFont(new Font("Tahoma", Font.BOLD, 9));
        lblAlreadyHaveAcc.setForeground(Color.WHITE);
        lblAlreadyHaveAcc.setBounds(287, 260, 135, 14);
        contentPane.add(lblAlreadyHaveAcc);
        
        lblAlreadyHaveAcc.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				dispose();

                LoginPage login = new LoginPage();
                login.setUndecorated(true); // Ẩn thanh tiêu đề
                login.setVisible(true);
				
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