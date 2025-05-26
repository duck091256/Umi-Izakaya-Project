package test;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.*;
import java.awt.*;
import javax.swing.*;

public class HighQualityChart {
    public static void main(String[] args) {
        // Tạo dataset cho doanh số trung bình tháng
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(5, "Doanh số trung bình (tháng)", "Món 1");
        dataset.addValue(8, "Doanh số trung bình (tháng)", "Món 2");
        dataset.addValue(6, "Doanh số trung bình (tháng)", "Món 3");
        dataset.addValue(7, "Doanh số trung bình (tháng)", "Món 4");
        dataset.addValue(12, "Doanh số trung bình (tháng)", "Món 5");

        // Tạo dataset cho doanh số ngày hôm nay
        dataset.addValue(3, "Doanh số hôm nay", "Món 1");
        dataset.addValue(15, "Doanh số hôm nay", "Món 2");
        dataset.addValue(7, "Doanh số hôm nay", "Món 3");
        dataset.addValue(9, "Doanh số hôm nay", "Món 4");
        dataset.addValue(20, "Doanh số hôm nay", "Món 5");

        // Tạo biểu đồ cột ngang (hàng ghép)
        JFreeChart chart = ChartFactory.createBarChart(
                "HÀNG BÁN CHẠY",    // Tiêu đề
                "Món ăn",           // Nhãn trục X
                "Doanh số (triệu VNĐ)", // Nhãn trục X
                dataset,
                PlotOrientation.HORIZONTAL,  // Chuyển thành biểu đồ ngang
                true, true, false);

        // Tùy chỉnh màu sắc
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        // Màu tươi tắn hơn
        renderer.setSeriesPaint(0, new Color(255, 102, 102));  // Đỏ nhạt
        renderer.setSeriesPaint(1, new Color(102, 178, 255));  // Xanh dương nhạt

        // Cải thiện hiển thị
        chart.setAntiAlias(true);
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 18));
        plot.getDomainAxis().setLabelFont(new Font("Arial", Font.BOLD, 14));
        plot.getRangeAxis().setLabelFont(new Font("Arial", Font.BOLD, 14));


        // Thay đổi trục X để hiển thị đơn vị "triệu VNĐ"
        NumberAxis xAxis = (NumberAxis) plot.getRangeAxis();
        xAxis.setLabel("Doanh số (triệu VNĐ)"); 
        xAxis.setTickLabelFont(new Font("Arial", Font.BOLD, 12));
        
        // Hiển thị biểu đồ
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(800, 500));
        
        JFrame frame = new JFrame("Biểu đồ bán chạy");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
