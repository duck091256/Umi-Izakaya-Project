package test;

import org.jfree.chart.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import java.awt.Color;

public class GroupedHorizontalBarChart {
    public static void main(String[] args) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Thêm dữ liệu (2 nhóm: Nam và Nữ)
        dataset.addValue(50, "Nam", "2020");
        dataset.addValue(70, "Nữ", "2020");
        dataset.addValue(80, "Nam", "2021");
        dataset.addValue(90, "Nữ", "2021");
        dataset.addValue(100, "Nam", "2022");
        dataset.addValue(120, "Nữ", "2022");

        // Tạo biểu đồ
        JFreeChart chart = ChartFactory.createBarChart(
                "Doanh thu theo năm", "Năm", "Doanh thu", dataset,
                org.jfree.chart.plot.PlotOrientation.HORIZONTAL, // Biểu đồ ngang
                true, true, false);

        // Tuỳ chỉnh màu sắc
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);  // Nam
        renderer.setSeriesPaint(1, Color.PINK);  // Nữ

        // Hiển thị biểu đồ
        ChartFrame frame = new ChartFrame("Biểu đồ cột ngang ghép nhóm", chart);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}
