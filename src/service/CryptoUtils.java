package service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

public class CryptoUtils {

    // Mã hóa dữ liệu bằng AES
    public static byte[] encryptAES(byte[] data, String password) throws Exception {
        SecretKeySpec key = new SecretKeySpec(getAesKey(password), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    // Giải mã dữ liệu bằng AES
    public static byte[] decryptAES(byte[] data, String password) throws Exception {
        SecretKeySpec key = new SecretKeySpec(getAesKey(password), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    // Lấy key AES chuẩn 16 bytes từ password
    private static byte[] getAesKey(String password) throws Exception {
        return Arrays.copyOf(password.getBytes("UTF-8"), 16); // AES cần key 128-bit
    }
}