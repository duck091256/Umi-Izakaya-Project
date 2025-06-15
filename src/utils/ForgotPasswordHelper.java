package utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.*;
import javax.swing.JOptionPane;

import data_access_object.LoginDAO;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

public class ForgotPasswordHelper {
    private static final String EMAIL_SENDER = "ductt.24it@vku.udn.vn";
    private static final String EMAIL_PASSWORD = "uazkrveusyrgxqcd"; // App password Gmail

    public static void startForgotPasswordFlow() {
        String email = JOptionPane.showInputDialog(null, "Nhập email đã đăng ký:", "Quên mật khẩu", JOptionPane.PLAIN_MESSAGE);

        if (email == null || email.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Email không được để trống.");
            return;
        }

        LoginDAO dao = new LoginDAO();
        String encryptedPassword = dao.getEncryptedPasswordByEmail(email);

        if (encryptedPassword == null) {
            JOptionPane.showMessageDialog(null, "Email không tồn tại trong hệ thống.");
            return;
        }

        String otp = generateOTP(6);
        sendOTPEmail(email, otp);

        String userOTP = JOptionPane.showInputDialog(null, "Nhập mã OTP đã gửi về email:", "Xác thực OTP", JOptionPane.PLAIN_MESSAGE);

        if (userOTP != null && userOTP.equals(otp)) {
            String newPassword = JOptionPane.showInputDialog(null, "Nhập mật khẩu mới:");
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                try {
                    String encryptedNewPassword = AESUtil.encrypt(newPassword);
                    dao.updatePasswordByEmail(email, encryptedNewPassword);
                    JOptionPane.showMessageDialog(null, "Mật khẩu đã được cập nhật!");
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Lỗi khi mã hóa mật khẩu mới.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Mật khẩu mới không hợp lệ.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Mã OTP không đúng.");
        }
    }

    public static String generateOTP(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static void sendOTPEmail(String toEmail, String otpCode) {
        String subject = "Mã OTP khôi phục mật khẩu";
        String body = "Mã OTP của bạn là: " + otpCode + "\nVui lòng không chia sẻ mã này với bất kỳ ai.";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_SENDER, EMAIL_PASSWORD);
            }
        });
        
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_SENDER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("OTP sent to email: " + toEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi gửi email OTP.");
        }
    }
}