package services;

import database.JDBCUtil;
import models.DetailReceipt;
import models.Dish;
import models.RankingStaff;
import models.Table;
import data_access_object.BillDAO;
import data_access_object.DetailReceiptDAO;
import data_access_object.RankingStaffDAO;
import data_access_object.SessionDAO;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class Payment {

    /**
     * Hàm xử lí dịch vụ thanh toán.
     *
     * Thanh toán dựa trên bàn đã order:
     * Tính tổng số tiền đã thanh toán,
     * Lấy billID từ tableBillMap,
     * Lấy người phụ trách hiện tại dựa vào table, sau đó
     * lần lượt ghi dữ liệu vào database (bill, detail_receipt, sessions)
     *
     * @param table bàn được yêu cầu thanh toán
     * @return true - nếu thành công, false - nếu thất bại
     */
    public static boolean payment(Table table) {
        if (table.getAvailable()) return false;

        String billID = Ordering.getBillIDForTable(table.getTableID());
        if (billID == null) {
            System.out.println("Không tìm thấy billID cho bàn " + table.getTableID());
            return false;
        }

        LocalDateTime time = LocalDateTime.now();
        ArrayList<Dish> list = Ordering.orderList.getOrDefault(table.getTableID(), new ArrayList<>());
        ArrayList<DetailReceipt> detailBill = new ArrayList<>();
        
        if (list.isEmpty() || list == null) {
        	System.out.println("Order list is null");
            return false;
        }

        list.sort(new Comparator<Dish>() {
            @Override
            public int compare(Dish o1, Dish o2) {
                return o1.getDishName().compareTo(o2.getDishName());
            }
        });

        double totalPrice = 0;
        int totalDishes = 0;

        // Gộp các món giống nhau
        HashMap<String, Integer> dishCount = new HashMap<>();
        for (Dish dish : list) {
            String dishID = dish.getDishID();
            int quantity = dish.getQuantity();
            totalPrice += dish.getDishPrice() * quantity;
            totalDishes += quantity;
            dishCount.put(dishID, dishCount.getOrDefault(dishID, 0) + quantity);
        }

        // Tạo danh sách DetailReceipt
        for (HashMap.Entry<String, Integer> entry : dishCount.entrySet()) {
            detailBill.add(new DetailReceipt(entry.getKey(), billID, entry.getValue()));
        }

        table.setAvailable(true);
        String staffID = table.getResponsibleBy();
        System.out.println(staffID);
        
        updateStaffRanking(staffID, totalDishes);

        Ordering.orderList.remove(table.getTableID());
        Ordering.removeBillIDForTable(table.getTableID());

        try (Connection conn = JDBCUtil.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu giao dịch
            
            BillDAO.updateBill(conn, billID, 1, time, totalPrice);
            DetailReceiptDAO.storeOrUpdateDetailReceipt(conn, detailBill);
            SessionDAO.getInstance().deleteSession(conn, table.getTableID());
            
            conn.commit(); // Hoàn thành giao dịch
            return true;
        } catch (Exception e) {
            System.out.println("Lỗi khi thanh toán: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Cập nhật ranking cho staff dựa trên totalSessions và totalDishes
     *
     * @param staffID ID của nhân viên
     * @param totalDishes Tổng số món đã phục vụ trong bill
     */
    private static void updateStaffRanking(String staffID, int totalDishes) {
        try {
            RankingStaff staff = RankingStaffDAO.getRankingStaffByID(staffID);
            int newTotalSessions = (staff != null ? staff.getTotalSessions() : 0) + 1;
            int newTotalDishes = (staff != null ? staff.getTotalDishes() : 0) + totalDishes;
            double rating = newTotalSessions * 1.5 + newTotalDishes;

            RankingStaffDAO.updateRankings(staffID, newTotalSessions, newTotalDishes, rating, 0);

            ArrayList<RankingStaff> allStaff = RankingStaffDAO.getAllRankingStaff();
            allStaff.removeIf(s -> s.getStaffID().equals(staffID));
            allStaff.add(new RankingStaff(staffID, newTotalSessions, newTotalDishes, rating, 0));
            
            allStaff.sort((s1, s2) -> Double.compare(
                s2.getTotalSessions() * 1.5 + s2.getTotalDishes(),
                s1.getTotalSessions() * 1.5 + s1.getTotalDishes()
            ));

            for (int i = 0; i < allStaff.size(); i++) {
                RankingStaff s = allStaff.get(i);
                if (s.getRanking() != i + 1) {
                    RankingStaffDAO.updateRankings(s.getStaffID(), s.getTotalSessions(), s.getTotalDishes(), s.getRating(), i + 1);
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi cập nhật ranking: " + e.getMessage());
            e.printStackTrace();
        }
    }
}