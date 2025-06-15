package services;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.*;
import java.util.Scanner;

public class MiniMapPreviewPanel extends JPanel {

    private final String API_KEY = "0ea468694a9c4965aaa7ee6b3590b93d";

    public MiniMapPreviewPanel() {
        setLayout(new BorderLayout());

        try {
            // Nhà hàng Umi Izakaya
            String destLat = "16.062964073199087";
            String destLon = "108.21937584478228";
            
            // Hiển thị ảnh bản đồ nhỏ từ Geoapify
            String mapUrl = String.format(
                "https://maps.geoapify.com/v1/staticmap?style=osm-carto&width=297&height=165&center=lonlat:%s,%s&zoom=16&marker=lonlat:%s,%s;type:material;color:%%23ff0000;size:medium&apiKey=%s",
                destLon, destLat, destLon, destLat, API_KEY
            );

            InputStream is = URI.create(mapUrl).toURL().openStream();
            BufferedImage image = ImageIO.read(is);
            JLabel label = new JLabel(new ImageIcon(image));
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Mở bản đồ lớn khi click
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    SwingUtilities.invokeLater(() -> new FullMapFrame());
                }
            });

            add(label, BorderLayout.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
            add(new JLabel("Không thể tải bản đồ nhỏ"));
        }
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Bản đồ nhỏ - Nhấn để xem");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(320, 240);
        f.add(new MiniMapPreviewPanel());
        f.setVisible(true);
    }
}