package fx;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AnimatedMoving {
    /**
     * Hàm tạo hiệu ứng thay đổi kích thước cho một thành phần.
     *
     * @param component   Thành phần cần thay đổi
     * @param startHeight Chiều cao ban đầu
     * @param targetHeight Chiều cao mục tiêu
     * @param updateAction Hành động cập nhật chiều cao
     */
    public static void animateResize(JComponent component, int startHeight, int targetHeight, ResizeUpdate updateAction) {
        Timer timer = new Timer(10, null); // Timer với mỗi bước 10ms
        long startTime = System.currentTimeMillis();
        int duration = 300; // Thời gian hiệu ứng (ms)

        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                double progress = Math.min(1.0, (double) elapsed / duration); // Tỉ lệ hoàn thành
                int currentHeight = (int) (startHeight + (targetHeight - startHeight) * progress);
                updateAction.update(currentHeight);

                if (progress >= 1.0) {
                    timer.stop();
                }
            }
        });

        timer.start();
    }

    /**
     * Hàm tạo hiệu ứng di chuyển cho một thành phần.
     *
     * @param component Thành phần cần di chuyển
     * @param startY    Vị trí Y ban đầu
     * @param targetY   Vị trí Y mục tiêu
     * @param updateAction Hành động cập nhật vị trí Y
     */
    public static void animateMove(JComponent component, int startY, int targetY, MoveUpdate updateAction) {
        Timer timer = new Timer(10, null); // Timer với mỗi bước 10ms
        long startTime = System.currentTimeMillis();
        int duration = 300; // Thời gian hiệu ứng (ms)

        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                double progress = Math.min(1.0, (double) elapsed / duration); // Tỉ lệ hoàn thành
                int currentY = (int) (startY + (targetY - startY) * progress);
                updateAction.update(currentY);

                if (progress >= 1.0) {
                    timer.stop();
                }
            }
        });

        timer.start();
    }

    // Functional interface cho hiệu ứng thay đổi kích thước
    public interface ResizeUpdate {
        void update(int currentHeight);
    }

    // Functional interface cho hiệu ứng di chuyển
    public interface MoveUpdate {
        void update(int currentY);
    }
}
