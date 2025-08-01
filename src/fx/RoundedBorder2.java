package fx;

import javax.swing.border.AbstractBorder;
import java.awt.*;

public class RoundedBorder2 extends AbstractBorder {
    private final int radius;

    public RoundedBorder2(int radius) {
        this.radius = radius;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ viền bo tròn
        g2.setColor(Color.GRAY);
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(radius, radius, radius, radius);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = radius;
        insets.right = radius;
        insets.top = radius;
        insets.bottom = radius;
        return insets;
    }
}
