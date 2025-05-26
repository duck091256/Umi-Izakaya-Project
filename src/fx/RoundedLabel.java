package fx;

import javax.swing.*;
import java.awt.*;

public class RoundedLabel extends JLabel {
	private static final long serialVersionUID = 1L;
	
    private int cornerRadius = 15; // Bán kính bo góc

    public RoundedLabel(String text) {
        super(text);
        setOpaque(false); // Để vẽ nền tùy chỉnh
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ nền với bo góc cả 4 phía
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        // Vẽ đường viền (nếu cần)
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);
    }
}
