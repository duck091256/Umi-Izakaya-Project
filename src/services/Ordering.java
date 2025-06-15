package services;

import data_access_object.SessionDAO;
import data_access_object.BillDAO;
import data_access_object.DetailReceiptDAO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import database.JDBCUtil;
import models.DetailReceipt;
import models.Dish;
import models.Session;
import models.Table;

import java.sql.Connection;

public class Ordering {
    public static HashMap<String, ArrayList<Dish>> orderList = new HashMap<>();
    public static HashMap<String, String> tableBillMap = new HashMap<>();
    
    /**
     * Gọi thêm món vào danh sách các món hiện tại của bàn hiện tại
     *
     * @param table bàn order món ăn
     * @param listDish Các món ăn được gọi thêm
     * @return true - nếu thành công, false - nếu thất bại
     */
    public static boolean order(Connection conn, Table table, ArrayList<Dish> listDish) {
        try {
            String tableID = table.getTableID();
            ArrayList<Dish> list = orderList.getOrDefault(tableID, new ArrayList<>());
            String billID = tableBillMap.get(tableID);

            if (billID == null) {
                billID = getBillID(LocalDateTime.now());
                BillDAO.storeBill(conn, billID, 0, LocalDateTime.now(), 0.0);
                Session session = new Session(0, table.getResponsibleBy(), tableID, billID);
                SessionDAO.getInstance().insertSession(conn, session);
                tableBillMap.put(tableID, billID);
            }

            list.addAll(listDish);
            HashMap<String, Integer> dishCount = new HashMap<>();
            for (Dish dish : list) {
                dishCount.put(dish.getDishID(), dishCount.getOrDefault(dish.getDishID(), 0) + dish.getQuantity());
            }
            ArrayList<DetailReceipt> detailReceipts = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : dishCount.entrySet()) {
                detailReceipts.add(new DetailReceipt(entry.getKey(), billID, entry.getValue()));
            }
            DetailReceiptDAO.storeOrUpdateDetailReceipt(conn, detailReceipts);
            orderList.put(tableID, list);
            table.setAvailable(false);
            return true;
        } catch (Exception e) {
            System.out.println("Lỗi khi thêm món: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Trả về danh sách các món được gọi của bàn hiện tại
     *
     * @param table bàn order món ăn
     * @return ArrayList<Dish> danh sách những món table đang order
     */
    public static ArrayList<Dish> getOrderingFromTable(Table table) {
        return orderList.getOrDefault(table.getTableID(), new ArrayList<>());
    }
    
    /**
     * Thêm một món ăn vào danh sách món của một bàn cụ thể.
     *
     * @param table Bàn đích để thêm món ăn
     * @param newDish  Món ăn cần thêm
     * @return true - nếu thành công, false - nếu xảy ra lỗi
     */
    public static boolean addDishToTable(Connection conn, Table table, Dish newDish) {
        try {
            String tableID = table.getTableID();
            ArrayList<Dish> currentOrder = orderList.getOrDefault(tableID, new ArrayList<>());
            String billID;

            // Tạo bill nếu bàn chưa có món nào
            if (currentOrder.isEmpty()) {
                billID = getBillID(LocalDateTime.now());
                BillDAO.storeBill(conn, billID, 0, LocalDateTime.now(), 0.0);
                Session session = new Session(0, table.getResponsibleBy(), tableID, billID);
                SessionDAO.getInstance().insertSession(conn, session);
                tableBillMap.put(tableID, billID);
            } else {
                billID = tableBillMap.get(tableID);
            }

            // Kiểm tra nếu món đã tồn tại, thì cập nhật quantity
            boolean dishExists = false;
            for (Dish d : currentOrder) {
                if (d.getDishID().equals(newDish.getDishID())) {
                    d.setQuantity(d.getQuantity() + newDish.getQuantity()); // Cập nhật số lượng trực tiếp
                    dishExists = true;
                    break;
                }
            }

            // Nếu món chưa có thì thêm mới
            if (!dishExists) {
                currentOrder.add(new Dish(
                    newDish.getDishID(),
                    newDish.getDishName(),
                    newDish.getDishPrice(),
                    newDish.getDishCategory(),
                    newDish.getDishImage(),
                    newDish.getQuantity()
                ));
            }

            // Cập nhật danh sách món cho bàn
            orderList.put(tableID, currentOrder);

            // Chuẩn bị dữ liệu để lưu DetailReceipt
            ArrayList<DetailReceipt> detailReceipts = new ArrayList<>();
            for (Dish d : currentOrder) {
                detailReceipts.add(new DetailReceipt(d.getDishID(), billID, d.getQuantity()));
            }
            DetailReceiptDAO.storeOrUpdateDetailReceipt(conn, detailReceipts);

            table.setAvailable(false);
            return true;

        } catch (Exception e) {
            System.out.println("Lỗi khi thêm món vào bàn: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Hàm tạo bill ID theo thời gian thực.
     *
     * @param time thời gian hiện tại
     * @return chuỗi billID được định dạng dựa theo thời gian thực
     */
    public static String getBillID(LocalDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH:mm:ss");
        return time.format(formatter);
    }

    /**
     * Lấy billID của bàn
     *
     * @param tableID ID của bàn
     * @return billID hoặc null nếu không tìm thấy
     */
    public static String getBillIDForTable(String tableID) {
        return tableBillMap.get(tableID);
    }

    /**
     * Xóa billID của bàn khi thanh toán xong
     *
     * @param tableID ID của bàn
     */
    public static void removeBillIDForTable(String tableID) {
        tableBillMap.remove(tableID);
    }
}