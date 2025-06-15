package controllers;

import data_access_object.TableDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import models.Table;
import services.Ordering;

public class HomeController {
    @FXML private Label tableNumber, tableStatus;
    @FXML private Button nextToCartBtn;
    @FXML private VBox tableCard;

    private String originalStatus;
    private static VBox currentlySelectedCard = null;
    private static Button currentlySelectedButton = null;
    private static String currentlySelectedStatus = null;
    private FoodOrderingController controller;
    
    public void setData(Table table) {
        tableNumber.setText(table.getTableID());

        originalStatus = Ordering.orderList.containsKey(table.getTableID())
                         ? "serving"
                         : "available";

        tableStatus.setText(originalStatus.equals("serving") ? "Đang phục vụ" : "Sẵn sàng");

        tableCard.getStyleClass().removeAll("available", "serving", "selected");
        tableCard.getStyleClass().add(originalStatus);
    }

    @FXML private void nextToCart() {
        if (currentlySelectedCard != null && currentlySelectedButton != null) {
            currentlySelectedCard.getStyleClass().remove("selected");
            currentlySelectedCard.getStyleClass().add(currentlySelectedStatus);
            currentlySelectedButton.setText("Chọn bàn");
        }

        tableCard.getStyleClass().removeAll("available", "serving", "selected");
        tableCard.getStyleClass().add("selected");
        nextToCartBtn.setText("Đang chọn");

        currentlySelectedCard = tableCard;
        currentlySelectedButton = nextToCartBtn;
        currentlySelectedStatus = originalStatus;

        if (controller != null) {
            Table table = new Table(tableNumber.getText(), "", "", "");
            table.setResponsibleBy(TableDAO.getStaffResponsible(tableNumber.getText()));
            table.setAvailable(false);	// Thiết lập bàn bận
            
            controller.setSelectedTable(table);
            controller.handleMenu();
        }
    }

    public void setCafeOrderingController(FoodOrderingController controller) {
        this.controller = controller;
    }
}