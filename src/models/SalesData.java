package models;

public class SalesData {
    private String dishName;
    private double avgMonthlyRevenue;
    private double revenueToday;

    public SalesData(String dishName, double avgMonthlyRevenue, double revenueToday) {
        this.dishName = dishName;
        this.avgMonthlyRevenue = avgMonthlyRevenue;
        this.revenueToday = revenueToday;
    }

    public String getDishName() {
        return dishName;
    }

    public double getAvgMonthlyRevenue() {
        return avgMonthlyRevenue;
    }

    public double getRevenueToday() {
        return revenueToday;
    }
}