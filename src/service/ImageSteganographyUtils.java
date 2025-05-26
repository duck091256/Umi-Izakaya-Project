package service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageSteganographyUtils {

    // Giấu tin vào ảnh (sử dụng LSB - Least Significant Bit)
    public static void hideMessage(BufferedImage image, String message, File output) throws IOException {
        // Chuyển sang ảnh dạng TYPE_INT_RGB để tránh lỗi khi ghi PNG
        BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        convertedImage.getGraphics().drawImage(image, 0, 0, null);

        byte[] messageBytes = message.getBytes();
        int messageLength = messageBytes.length;

        if ((messageLength + 4) * 8 > convertedImage.getWidth() * convertedImage.getHeight()) {
            throw new IllegalArgumentException("Ảnh quá nhỏ để chứa thông điệp.");
        }

        int idx = 0;

        // Encode độ dài tin nhắn (4 byte đầu)
        for (int i = 0; i < 4; i++) {
            int b = (messageLength >>> (i * 8)) & 0xFF;
            for (int bit = 7; bit >= 0; bit--) {
                int x = idx % convertedImage.getWidth();
                int y = idx / convertedImage.getWidth();
                int pixel = convertedImage.getRGB(x, y);

                int lsb = (b >>> bit) & 1;
                int newPixel = (pixel & 0xFFFFFFFE) | lsb;
                convertedImage.setRGB(x, y, newPixel);
                idx++;
            }
        }

        // Encode nội dung tin nhắn
        for (byte b : messageBytes) {
            for (int bit = 7; bit >= 0; bit--) {
                int x = idx % convertedImage.getWidth();
                int y = idx / convertedImage.getWidth();
                int pixel = convertedImage.getRGB(x, y);

                int lsb = (b >>> bit) & 1;
                int newPixel = (pixel & 0xFFFFFFFE) | lsb;
                convertedImage.setRGB(x, y, newPixel);
                idx++;
            }
        }

        ImageIO.write(convertedImage, "png", output);
    }

    // Giải mã tin từ ảnh
    public static String extractMessage(BufferedImage image) throws IOException {
        int idx = 0;
        int messageLength = 0;

        // Decode độ dài tin nhắn
        for (int i = 0; i < 4; i++) {
            int b = 0;
            for (int bit = 0; bit < 8; bit++) {
                int x = idx % image.getWidth();
                int y = idx / image.getWidth();
                int pixel = image.getRGB(x, y);
                b = (b << 1) | (pixel & 1);
                idx++;
            }
            messageLength |= (b << (i * 8));
        }

        byte[] messageBytes = new byte[messageLength];
        for (int i = 0; i < messageLength; i++) {
            int b = 0;
            for (int bit = 0; bit < 8; bit++) {
                int x = idx % image.getWidth();
                int y = idx / image.getWidth();
                int pixel = image.getRGB(x, y);
                b = (b << 1) | (pixel & 1);
                idx++;
            }
            messageBytes[i] = (byte) b;
        }

        return new String(messageBytes);
    }
} 