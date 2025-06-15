package views;

import javax.swing.*;

import data_access_object.*;
import utils.AESUtil;
import utils.ForgotPasswordHelper;

import java.awt.*;

public class ForgotPasswordDialog extends JDialog {
    private JTextField emailField;
    private JTextField otpField;
    private JPasswordField newPasswordField;
    private JButton sendOtpButton;
    private JButton resetPasswordButton;

    private String generatedOTP;
    private String userEmail;

    public ForgotPasswordDialog(Frame parent) {
        super(parent, "Quên mật khẩu", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);

        emailField = new JTextField();
        emailField.setBounds(198, 1, 188, 44);
        otpField = new JTextField();
        otpField.setBounds(198, 102, 188, 44);
        newPasswordField = new JPasswordField();
        newPasswordField.setBounds(198, 157, 188, 44);
        sendOtpButton = new JButton("Gửi mã OTP");
        sendOtpButton.setBounds(0, 55, 386, 33);
        resetPasswordButton = new JButton("Đặt lại mật khẩu");
        resetPasswordButton.setBounds(0, 208, 386, 33);
        getContentPane().setLayout(null);

        JLabel label = new JLabel("Nhập email đã đăng ký:");
        label.setBounds(0, 1, 188, 44);
        getContentPane().add(label);
        getContentPane().add(emailField);
        JLabel label_1 = new JLabel("Nhập mã OTP và mật khẩu mới:");
        label_1.setBounds(0, 102, 188, 44);
        getContentPane().add(label_1);
        getContentPane().add(sendOtpButton);
        getContentPane().add(newPasswordField);
        getContentPane().add(otpField);
        getContentPane().add(resetPasswordButton);

        otpField.setEnabled(false);
        newPasswordField.setEnabled(false);
        resetPasswordButton.setEnabled(false);

        sendOtpButton.addActionListener(e -> handleSendOTP());
        resetPasswordButton.addActionListener(e -> handleResetPassword());
    }

    private void handleSendOTP() {
        userEmail = emailField.getText().trim();
        if (userEmail.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập email.");
            return;
        }

        LoginDAO dao = new LoginDAO();
        if (dao.getEncryptedPasswordByEmail(userEmail) == null) {
            JOptionPane.showMessageDialog(this, "Email không tồn tại trong hệ thống.");
            return;
        }

        generatedOTP = ForgotPasswordHelper.generateOTP(6);
        ForgotPasswordHelper.sendOTPEmail(userEmail, generatedOTP);

        JOptionPane.showMessageDialog(this, "Mã OTP đã được gửi tới email của bạn.");
        otpField.setEnabled(true);
        newPasswordField.setEnabled(true);
        resetPasswordButton.setEnabled(true);
    }

    private void handleResetPassword() {
        String enteredOTP = otpField.getText().trim();
        String newPassword = new String(newPasswordField.getPassword());

        if (!enteredOTP.equals(generatedOTP)) {
            JOptionPane.showMessageDialog(this, "Mã OTP không chính xác.");
            return;
        }

        if (newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mật khẩu mới không được để trống.");
            return;
        }

        try {
            String encryptedPassword = AESUtil.encrypt(newPassword);
            LoginDAO dao = new LoginDAO();
            boolean success = dao.updatePasswordByEmail(userEmail, encryptedPassword);
            if (success) {
                JOptionPane.showMessageDialog(this, "Mật khẩu đã được cập nhật thành công!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật mật khẩu thất bại.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi mã hóa mật khẩu.");
        }
    }

    public static void showDialog(Frame parent) {
        ForgotPasswordDialog dialog = new ForgotPasswordDialog(parent);
        dialog.setVisible(true);
    }
}
