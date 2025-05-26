package fx;

import java.awt.*;
import javax.swing.border.Border;

public class RoundedBorderPanel implements Border {
    private int radius;      // Bán kính của viền bo tròn
    private Color color;     // Màu của viền
    private int thickness;   // Độ dày của viền

    public RoundedBorderPanel(int radius, Color color, int thickness) {
        this.radius = radius;
        this.color = color;
        this.thickness = thickness;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(thickness, thickness, thickness, thickness);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Thiết lập màu và độ dày nét vẽ
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(thickness));

        // Vẽ viền bo tròn
        g2d.drawRoundRect(
            x + thickness / 2,                // Dịch chuyển theo độ dày để không cắt viền
            y + thickness / 2, 
            width - thickness, 
            height - thickness, 
            radius, radius
        );
    }
}

