package service;

import java.util.*;
import java.util.stream.Collectors;

import data_access_object.BillDAO;
import data_access_object.DetailReceiptDAO;
import data_access_object.DishDAO;
import model.Bill;
import model.DetailReceipt;
import model.Dish;
import model.SalesData;

public class SalesAnalyzer {    
    public static List<SalesData> getTop10BestSellingDishes() {
        HashMap<String, Integer> dishSales = new HashMap<>();
        HashMap<String, Double> revenueLast30DaysMap = new HashMap<>();
        HashMap<String, Double> revenueTodayMap = new HashMap<>();
        HashMap<String, Integer> daysWithSalesMap = new HashMap<>();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date today = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        Date last30Days = cal.getTime();

        // Tính tổng số lượng món ăn đã bán từ trước đến nay
        for (DetailReceipt detail : DetailReceiptDAO.list) {
            dishSales.put(detail.getDishID(), dishSales.getOrDefault(detail.getDishID(), 0) + detail.getDishQuantity());
        }

        // Lấy 7 món bán chạy nhất
        List<String> topDishes = dishSales.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(7)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (DishDAO.map == null || DishDAO.map.isEmpty()) {
            System.err.println("⚠ Lỗi: DishDAO.map chưa được khởi tạo hoặc rỗng! Đang tải dữ liệu...");
            DishDAO.loadData();
        }

        if (BillDAO.list == null || BillDAO.list.isEmpty()) {
    	    System.err.println("⚠ Lỗi: BillDAO.list chưa được khởi tạo hoặc rỗng! Đang tải dữ liệu...");
    	    BillDAO.loadData();
    	}
        
        // Dùng Map để theo dõi các món ăn đã bán trong từng ngày
        Map<Date, Set<String>> dishesSoldPerDay = new HashMap<>();

        for (Bill bill : BillDAO.list) {
            Date billDate = bill.getTime();
            boolean isToday = isSameDay(billDate, today);
            boolean isInLast30Days = billDate.after(last30Days) && billDate.before(today);

            // Chuẩn hóa billDate về 0h0m0s để chỉ quan tâm đến ngày, không tính theo giờ
            Calendar billCal = Calendar.getInstance();
            billCal.setTime(billDate);
            billCal.set(Calendar.HOUR_OF_DAY, 0);
            billCal.set(Calendar.MINUTE, 0);
            billCal.set(Calendar.SECOND, 0);
            billCal.set(Calendar.MILLISECOND, 0);
            Date normalizedBillDate = billCal.getTime();

            // Nếu chưa có ngày này trong Map, thêm vào
            if (!dishesSoldPerDay.containsKey(normalizedBillDate)) {
                dishesSoldPerDay.put(normalizedBillDate, new HashSet<>());
            }

            for (DetailReceipt detail : bill.getDetailList()) {
                String dishID = detail.getDishID();
                double totalPrice = detail.getDishQuantity() * DishDAO.map.getOrDefault(dishID, new Dish()).getDishPrice();

                if (isToday) {
                    revenueTodayMap.put(dishID, revenueTodayMap.getOrDefault(dishID, 0.0) + totalPrice);
                }
                if (isInLast30Days) {
                    revenueLast30DaysMap.put(dishID, revenueLast30DaysMap.getOrDefault(dishID, 0.0) + totalPrice);

                    // Chỉ đếm món ăn một lần cho mỗi ngày
                    if (!dishesSoldPerDay.get(normalizedBillDate).contains(dishID)) {
                        daysWithSalesMap.put(dishID, daysWithSalesMap.getOrDefault(dishID, 0) + 1);
                        dishesSoldPerDay.get(normalizedBillDate).add(dishID); // Đánh dấu món ăn đã bán trong ngày này
                    }
                }
            }
        }

        List<SalesData> top10Sales = new ArrayList<>();

        // Duyệt danh sách món ăn để tính toán kết quả
        for (String dishID : topDishes) {
            Dish dish = DishDAO.map.get(dishID);
            if (dish == null) continue;

            double revenueToday = revenueTodayMap.getOrDefault(dishID, 0.0);
            double revenueLast30Days = revenueLast30DaysMap.getOrDefault(dishID, 0.0);
            int daysWithSales = daysWithSalesMap.getOrDefault(dishID, 1); // Tránh chia cho 0

            double avgMonthlyRevenue = revenueLast30Days / daysWithSales;

            top10Sales.add(new SalesData(dish.getDishName(), avgMonthlyRevenue, revenueToday));         
        }

        
        return top10Sales;
    }

    public static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
