package fx;
import javax.swing.*;
import java.awt.*;

public class RoundedLabelEffect extends JLabel {
    private int cornerRadius = 15; // Bán kính bo góc

    public RoundedLabelEffect(String text) {
        super(text);
        setOpaque(false);
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ nền với bo góc chỉ ở phía trên
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight() + cornerRadius, cornerRadius, cornerRadius);
        g2.fillRect(0, cornerRadius, getWidth(), getHeight() - cornerRadius); // Phần dưới không bo

        // Vẽ đường viền (nếu cần)
        g2.setColor(getForeground());
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1 + cornerRadius, cornerRadius, cornerRadius);
        g2.drawLine(0, cornerRadius, 0, getHeight()); // Đường thẳng góc trái
        g2.drawLine(getWidth() - 1, cornerRadius, getWidth() - 1, getHeight()); // Đường thẳng góc phải

        g2.dispose();
        super.paintComponent(g);
    }
}
