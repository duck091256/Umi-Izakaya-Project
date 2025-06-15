package models;

public class Dish {
    private String dishID;
    private String dishName;
    private double dishPrice;
    private String dishCategory;
    private String dishImage;
    private int quantity;

    public Dish(String dishID, String dishName, double dishPrice, String dishCategory, String dishImage, int quantity) {
        this.dishID = dishID;
        this.dishName = dishName;
        this.dishPrice = dishPrice;
        this.dishCategory = dishCategory;
        this.dishImage = dishImage;
        this.quantity = quantity;
    }
    
    // Hàm khởi tạo không có quantity, mặc định quantity = 1
    public Dish(String dishID, String dishName, double dishPrice, String dishCategory, String dishImage) {
        this(dishID, dishName, dishPrice, dishCategory, dishImage, 1);
    }
    
    public Dish() {}

    public String getDishID() {
        return dishID;
    }

    public void setDishID(String dishID) {
        this.dishID = dishID;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public double getDishPrice() {
        return dishPrice;
    }

    public void setDishPrice(double dishPrice) {
        this.dishPrice = dishPrice;
    }

    public String getDishCategory() {
        return dishCategory;
    }

    public void setDishCategory(String dishCategory) {
        this.dishCategory = dishCategory;
    }

    public String getDishImage() {
        return dishImage;
    }

    public void setDishImage(String dishImage) {
        this.dishImage = dishImage;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}