package controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import models.Dish;

public class DishItemController {
    @FXML private ImageView dishImage;
    @FXML private Label dishName, dishPrice, quantityLabel;
    @FXML private Button addToCartBtn;
    @FXML private HBox quantityBox; // Thêm tham chiếu đến HBox chứa nút tăng/giảm
    private int quantity = 1;
    private FoodOrderingController controller;
    private Dish dish;

    public void setData(Dish dish) {
        this.dish = dish;
        dishName.setText(dish.getDishName());
        dishPrice.setText(String.format("%.3f", dish.getDishPrice()) + " VNĐ");
        
        String imagePath = dish.getDishImage().replace("src/", "");
        Image img = new Image(getClass().getResourceAsStream("/" + imagePath));
        dishImage.setImage(img);

        // Ẩn HBox và Button nếu chưa chọn bàn
        updateUI();
        
        // Thêm sự kiện preview ảnh khi bấm vào dishImage
        dishImage.setOnMouseClicked(event -> showImagePreview(img));
    }

    @FXML private void decreaseQty() {
        if (quantity > 1) {
            quantity--;
            quantityLabel.setText(String.valueOf(quantity));
        }
    }

    @FXML private void increaseQty() {
        quantity++;
        quantityLabel.setText(String.valueOf(quantity));
    }
    
    @FXML private void resetQty() {
    	quantity = 1;
    	quantityLabel.setText(String.valueOf(quantity));
    }
    
    @FXML private void addToCart() {
        if (controller != null && dish != null) {
            if (controller.getSelectedTable() == null) {
                showAlert(Alert.AlertType.WARNING, "Chưa chọn bàn", "Vui lòng chọn bàn trước khi thêm món!");
                return;
            }
            dish.setQuantity(quantity);
            controller.addToCart(dish.getDishID(), quantity);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm " + dish.getDishName() + " x" + quantity + " vào giỏ hàng!");
            
            resetQty();
        }
    }

    public void setCafeOrderingController(FoodOrderingController controller) {
        this.controller = controller;
        updateUI();
    }

    protected void updateUI() {
        if (controller != null && controller.getSelectedTable() == null) {
            quantityBox.setVisible(false); 	// Ẩn HBox chứa nút tăng/giảm
            quantityBox.setManaged(false); 	// Ẩn không gian layout khỏi parent
            addToCartBtn.setVisible(false); // Ẩn nút "Thêm vào giỏ hàng"
            addToCartBtn.setManaged(false); 
        } else {
            quantityBox.setVisible(true); 	 // Hiện HBox
            quantityBox.setManaged(true);
            addToCartBtn.setVisible(true); 	 // Hiện nút
            addToCartBtn.setManaged(true);
        }
    }

    private void showImagePreview(Image image) {
        Stage previewStage = new Stage();
        previewStage.initModality(Modality.APPLICATION_MODAL);
        previewStage.setTitle("Xem trước ảnh món ăn");

        ImageView previewImage = new ImageView(image);
        previewImage.setFitWidth(500);
        previewImage.setPreserveRatio(true);

        VBox layout = new VBox(10);
        layout.getChildren().add(previewImage);
        layout.setStyle("-fx-padding: 10; -fx-alignment: center;");

        Scene scene = new Scene(layout, 550, 450);
        previewStage.setScene(scene);
        previewStage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}