package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import data_access_object.*;
import database.JDBCUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.*;
import services.Ordering;
import services.Payment;
import test.EnhancedChatClient;
import views.LoginPage;
import views.ManagementSystem;

public class FoodOrderingController {

    @FXML private ImageView logo, avatar;
    @FXML private TextField searchBar;
    @FXML private Label employeeName, tableLabel;
    @FXML private Button notificationBtn, chatBtn;
    @FXML private GridPane tableSelectionPane;
    @FXML private ListView<String> cartList;
    @FXML private HBox rootContainer;
    @FXML private VBox mainContent;
    @FXML private VBox cartBox;
    @FXML private Label titleCenter;
    @FXML private ImageView homeIcon, menuIcon, orderIcon, historyIcon, logoutIcon;
    @FXML private Label homeLabel, menuLabel, orderLabel, historyLabel, logoutLabel;
    
    private String username;
    private String staffID;
    private String staffFullname;
    private Table selectedTable;
    private List<DishItemController> dishItemControllers = new ArrayList<>(); // Lưu danh sách controller

    public void setAccountInformation(String username) {
        this.username = username;
        this.staffID = StaffDAO.getStaffIDByUsername(username);
        this.staffFullname = StaffDAO.getStaffNameFromDatabaseByID(staffID);
        
        if (staffID == null) {
            System.out.println("Không tìm thấy staffID cho username: " + username);
        }
        employeeName.setText(staffFullname);
        System.out.println("Đăng nhập với tài khoản: " + username + ", staffID: " + staffID);
    }
    
    @FXML
    public void initialize() {
        HBox.setHgrow(mainContent, Priority.ALWAYS);
        HBox.setHgrow(cartBox, Priority.ALWAYS);
        
        loadTable();
        
        // Hover cho icon
        homeLabel.setOnMouseEntered(event -> {
        	homeIcon.setImage(new Image(getClass().getResource("/icon/icons8-home-498.png").toExternalForm()));
        });

        homeLabel.setOnMouseExited(event -> {
        	homeIcon.setImage(new Image(getClass().getResource("/icon/icons8-home-144.png").toExternalForm()));
        });
        
        menuLabel.setOnMouseEntered(event -> {
        	menuIcon.setImage(new Image(getClass().getResource("/icon/icons8-grid-2-96.png").toExternalForm()));
        });

        menuLabel.setOnMouseExited(event -> {
        	menuIcon.setImage(new Image(getClass().getResource("/icon/icons8-grid-2-100.png").toExternalForm()));
        });

        orderLabel.setOnMouseEntered(event -> {
        	orderIcon.setImage(new Image(getClass().getResource("/icon/icons8-favorite-500.png").toExternalForm()));
        });

        orderLabel.setOnMouseExited(event -> {
        	orderIcon.setImage(new Image(getClass().getResource("/icon/icons8-favorite-480.png").toExternalForm()));
        });
        
        historyLabel.setOnMouseEntered(event -> {
        	historyIcon.setImage(new Image(getClass().getResource("/icon/icons8-paper-500.png").toExternalForm()));
        });

        historyLabel.setOnMouseExited(event -> {
        	historyIcon.setImage(new Image(getClass().getResource("/icon/icons8-paper-144.png").toExternalForm()));
        });
        
        logoutLabel.setOnMouseEntered(event -> {
        	logoutIcon.setImage(new Image(getClass().getResource("/icon/icons8-logout-96.png").toExternalForm()));
        });

        logoutLabel.setOnMouseExited(event -> {
        	logoutIcon.setImage(new Image(getClass().getResource("/icon/icons8-logout-95.png").toExternalForm()));
        });
        
        logoutLabel.setOnMouseClicked(event -> {
            // Đóng cửa sổ JavaFX hiện tại
        	((Stage) logoutLabel.getScene().getWindow()).close(); // chỉ đóng cửa sổ hiện tại

            // Gọi Swing UI
            SwingUtilities.invokeLater(() -> {
            	LoginPage loginPage = new LoginPage(); // class bạn tạo cho giao diện Swing
            	loginPage.setUndecorated(true);
            	loginPage.setVisible(true);
            });
        });
    }

    @FXML
    private void handleHome() {
        loadTable();
        titleCenter.setText("Chào mừng trở lại – ようこそ Umi Izakaya!");
        System.out.println("Quay về trang chủ!");
        updateCartDisplay(null);
    }

    @FXML
    protected void handleMenu() {
        titleCenter.setText("Ẩm thực Nhật – Đậm đà từng món ăn");
        if (selectedTable != null) {
            loadDish();
            System.out.println("Hiển thị menu...");
            tableLabel.setText("Thông tin của bàn: " + selectedTable.getTableID());
        } else {
            loadDish(); // Cho phép xem menu ngay cả khi chưa chọn bàn
            tableLabel.setText("Bàn: Chưa chọn");
            System.out.println("Hiển thị menu...");
        }
    }

    @FXML
    private void handleHistory() {  	
        titleCenter.setText("Lịch sử giao dịch");
        tableSelectionPane.getChildren().clear();

        DetailReceiptDAO.loadData();
        BillDAO.loadData();
        
        ObservableList<Bill> billData = FXCollections.observableArrayList(BillDAO.list);

        TableView<Bill> tableView = new TableView<>();
        tableView.setPrefWidth(700);
        tableView.setPrefHeight(520);

        TableColumn<Bill, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("billID"));

        TableColumn<Bill, Double> totalAmountColumn = new TableColumn<>("Total Amount");
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("payment"));

        TableColumn<Bill, String> orderDateColumn = new TableColumn<>("Order Date");
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

        TableColumn<Bill, Void> detailsColumn = new TableColumn<>("Details");
        detailsColumn.setCellFactory(param -> new TableCell<Bill, Void>() {
            private final Button detailsButton = new Button("View Details");

            {
                detailsButton.setOnAction(event -> {
                    Bill bill = getTableView().getItems().get(getIndex());
                    System.out.println("Viewing details for Bill ID: " + bill.getBillID());

                    List<DetailReceipt> details = DetailReceiptDAO.getDetailReceiptList(bill.getBillID());

                    if (details != null && !details.isEmpty()) {
                        tableSelectionPane.getChildren().clear();
                        ListView<String> detailListView = new ListView<>();
                        ObservableList<String> detailItems = FXCollections.observableArrayList();
                        
                        DishDAO.loadData();
                        for (DetailReceipt detail : details) {
                            Dish dish = DishDAO.list.stream()
                                    .filter(d -> d.getDishID().equals(detail.getDishID()))
                                    .findFirst()
                                    .orElse(null);
                            if (dish != null) {
                                String item = dish.getDishName() + " - Quantity: " + detail.getDishQuantity() +
                                        " - Price: " + dish.getDishPrice() * detail.getDishQuantity() + "00 VNĐ";
                                detailItems.add(item);
                            }
                        }
                        
                        detailListView.setItems(detailItems);
                        detailListView.setPrefWidth(700);
                        detailListView.setPrefHeight(520);
                        tableSelectionPane.add(detailListView, 0, 0);
                    } else {
                        System.out.println("No details found for Bill ID: " + bill.getBillID());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailsButton);
                }
            }
        });
        
        idColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.35));
        totalAmountColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25));
        orderDateColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25));
        detailsColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));

        tableView.getColumns().addAll(idColumn, totalAmountColumn, orderDateColumn, detailsColumn);
        tableView.setItems(billData);
        tableSelectionPane.add(tableView, 0, 0);

        System.out.println("Hiển thị lịch sử hóa đơn...");
    }

    @FXML
    private void chatSystem() {
        new Thread(() -> {
            System.out.println("Chat system started for: " + staffFullname);
            EnhancedChatClient.main(new String[]{staffFullname});
        }).start();
    }
    
    @FXML
    private void checkout() {
        if (selectedTable != null) {
            if (Payment.payment(selectedTable)) {
                showAlert(Alert.AlertType.INFORMATION, "Thanh toán thành công", "Thanh toán thành công cho bàn: " + selectedTable.getTableID());
                
                updateCartDisplay(Ordering.getOrderingFromTable(selectedTable));
                loadTable();
            } else {
                showAlert(Alert.AlertType.ERROR, "Thanh toán thất bại", "Thanh toán thất bại cho bàn: " + selectedTable.getTableID());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn bàn", "Vui lòng chọn bàn trước khi thanh toán!");
        }
    }

    public Table getSelectedTable() {
        return selectedTable;
    }

    public void setSelectedTable(Table table) {
        this.selectedTable = table;
        if (table != null) {
            tableLabel.setText("Thông tin của bàn: " + table.getTableID());
        } else {
            tableLabel.setText("Bàn: Chưa chọn");
        }

        ArrayList<Dish> orderList = Ordering.getOrderingFromTable(table);
        updateCartDisplay(orderList);

        // Cập nhật giao diện của các DishItemController
        for (DishItemController dishController : dishItemControllers) {
            dishController.updateUI();
        }
    }

    public void addToCart(String dishId, int quantity) {
        if (selectedTable != null) {
            Dish dish = DishDAO.list.stream()
                    .filter(d -> d.getDishID().equals(dishId))
                    .findFirst()
                    .orElse(null);
            if (dish != null) {
                dish.setQuantity(quantity);
                try (Connection conn = JDBCUtil.getConnection()) {
                    if (Ordering.addDishToTable(conn, selectedTable, dish)) {
                        updateCartDisplay(Ordering.getOrderingFromTable(selectedTable));
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Thêm món thất bại", "Không thể thêm món " + dish.getDishName() + " vào giỏ hàng!");
                    }
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Đã xảy ra lỗi khi thêm món: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Món không tồn tại", "Không tìm thấy món với ID: " + dishId);
            }
        }
    }

    private void updateCartDisplay(ArrayList<Dish> orderList) {
        ObservableList<String> cartItemsDisplay = FXCollections.observableArrayList();
        if (orderList != null) {
            for (Dish dish : orderList) {
                String item = dish.getDishName() + " x" + dish.getQuantity() + 
                             " - " + (dish.getDishPrice() * dish.getQuantity()) + "00 VNĐ";
                cartItemsDisplay.add(item);
            }
        }
        cartList.setItems(cartItemsDisplay);
        cartList.setPrefWidth(300);
        cartList.setPrefHeight(520);
        cartList.setStyle("""
        	    -fx-font-size: 16px;
        	    -fx-padding: 10px;
        	    -fx-cell-size: 40px; /* khoảng cách dòng */
        	""");
    }

    public void loadTable() {
        tableSelectionPane.getChildren().clear();

        StaffDAO.loadData();
        TableDAO.loadData();
        int column = 0;
        int row = 0;

        final int MAX_COLUMNS = 4;

        for (Table table : TableDAO.list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
                AnchorPane tableCard = loader.load();

                HomeController controller = loader.getController();
                controller.setData(table);
                controller.setCafeOrderingController(this);

                tableSelectionPane.add(tableCard, column, row);

                column++;
                if (column == MAX_COLUMNS) {
                    column = 0;
                    row++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        selectedTable = null;
        tableLabel.setText("Bàn: Chưa chọn");
        updateCartDisplay(null);
    }
    
    public void loadDish() {
        tableSelectionPane.getChildren().clear();
        dishItemControllers.clear(); // Xóa danh sách cũ

        DishDAO.loadData();
        if (DishDAO.list.isEmpty()) {
            System.out.println("No dishes loaded in DishDAO!");
            return;
        }

        int column = 0;
        int row = 0;

        final int MAX_COLUMNS = 3;

        for (Dish dish : DishDAO.list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dish_item.fxml"));
                AnchorPane dishCard = loader.load();
                DishItemController controller = loader.getController();
                controller.setData(dish);
                controller.setCafeOrderingController(this);
                dishItemControllers.add(controller); // Lưu controller để cập nhật sau

                tableSelectionPane.add(dishCard, column, row);

                column++;
                if (column == MAX_COLUMNS) {
                    column = 0;
                    row++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public String getStaffID() {
        return staffID;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}