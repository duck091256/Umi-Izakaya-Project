package test;

import org.jfree.chart.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import java.awt.Color;

public class HorizontalBarChartExample {
    public static void main(String[] args) {
        // Tạo dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(50, "Doanh thu", "2020");
        dataset.addValue(75, "Doanh thu", "2021");
        dataset.addValue(100, "Doanh thu", "2022");

        // Tạo biểu đồ cột ngang
        JFreeChart chart = ChartFactory.createBarChart(
                "Doanh thu theo năm",  // Tiêu đề
                "Năm",  // Nhãn trục X
                "Doanh thu",  // Nhãn trục Y
                dataset,
                org.jfree.chart.plot.PlotOrientation.HORIZONTAL, // Đặt là HORIZONTAL để làm row chart
                false, true, false
        );

        // Tuỳ chỉnh màu sắc
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.BLUE); // Màu xanh cho cột

        // Hiển thị biểu đồ
        ChartFrame frame = new ChartFrame("Biểu đồ cột ngang", chart);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}
