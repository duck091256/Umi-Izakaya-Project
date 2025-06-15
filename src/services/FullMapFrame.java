package services;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.*;
import org.jxmapviewer.input.*;
import org.jxmapviewer.painter.*;
import org.jxmapviewer.painter.Painter;

import javax.swing.*;
import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.List;

public class FullMapFrame extends JFrame {

    public FullMapFrame() {
        setTitle("Bản đồ tương tác - Umi Izakaya");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JXMapViewer mapViewer = new JXMapViewer();
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);

        // 🧭 Vị trí hiện tại
        String[] current = getCurrentLocationCoords();
        String latStr = current[0].replaceAll("[^0-9.\\-]", "");
        String lonStr = current[1].replaceAll("[^0-9.\\-]", "");
        GeoPosition currentPos = new GeoPosition(Double.parseDouble(latStr), Double.parseDouble(lonStr));

        // 🏮 Nhà hàng Umi Izakaya
        GeoPosition destPos = new GeoPosition(16.062964073199087, 108.21937584478228);

        // Center + Zoom
        mapViewer.setZoom(5);
        mapViewer.setAddressLocation(destPos);

        // Waypoints
        Set<Waypoint> waypoints = new HashSet<>();
        waypoints.add(new DefaultWaypoint(currentPos));
        waypoints.add(new DefaultWaypoint(destPos));

        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypoints);

        // Zoom + pan
        PanMouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));

        // Combine painters
        List<Painter<JXMapViewer>> painters = new ArrayList<>();
        painters.add(waypointPainter);
        mapViewer.setOverlayPainter(new CompoundPainter<>(painters));

        add(mapViewer, BorderLayout.CENTER);

        // 🔗 Nút mở Google Maps chỉ đường ngoài
        JButton btnDirection = new JButton("Mở Google Maps chỉ đường");
        btnDirection.addActionListener(e -> {
            try {
                String gmapsUrl = String.format(
                    "https://www.google.com/maps/dir/?api=1&origin=%s,%s&destination=%s,%s",
                    current[0], current[1], "16.062964073199087", "108.21937584478228"
                );
                Desktop.getDesktop().browse(new URI(gmapsUrl));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(btnDirection);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
        setLocationRelativeTo(null);
    }

    // 🛰️ Lấy vị trí hiện tại từ IP
    public static String[] getCurrentLocationCoords() {
        try {
            String apiKey = "0ea468694a9c4965aaa7ee6b3590b93d";	
            URL url = URI.create("https://api.geoapify.com/v1/ipinfo?apiKey=" + apiKey).toURL();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();

            Scanner sc = new Scanner(con.getInputStream());
            StringBuilder json = new StringBuilder();
            while (sc.hasNext()) {
                json.append(sc.nextLine());
            }
            sc.close();

            String data = json.toString();
            // Trích xuất vị trí từ JSON
            String lat = data.split("\"latitude\":")[1].split(",")[0].replaceAll("[^0-9.\\-]", "");
            String lon = data.split("\"longitude\":")[1].split(",")[0].replaceAll("[^0-9.\\-]", "");

            return new String[]{lat.trim(), lon.trim()};

        } catch (Exception e) {
            e.printStackTrace();
            // fallback về Đà Nẵng
            System.out.println("Trả về Đà Nẵng");
            return new String[]{"16.0471", "108.2062"};
        }
    }
}