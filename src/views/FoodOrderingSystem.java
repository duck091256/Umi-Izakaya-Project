package views;

import controllers.FoodOrderingController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;
import test.EnhancedChatServer;

public class FoodOrderingSystem {
    private String username;

    public FoodOrderingSystem(String username) {
        this.username = username;
    }

    public void show() {
        Platform.runLater(() -> {
            try {
        		// Khởi chạy server trong 1 thread riêng
                new Thread(() -> EnhancedChatServer.main(null)).start();
                
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FoodOrdering.fxml"));
                Parent root = loader.load();

                // Truyền dữ liệu vào controller
                FoodOrderingController controller = loader.getController();
                controller.setAccountInformation(username);

                Stage stage = new Stage();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
                stage.setTitle("UMI IZAKAYA");
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
