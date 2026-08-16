package com.bank;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    private void showSearchResults() {
        loadSubView("/com/bank/views/GlobalSearch.fxml");
    }

    @FXML
    private void showHomeView() {
        loadSubView("/com/bank/views/HomeView.fxml");
    }

    @FXML
    private void showActionsView() {
        loadSubView("/com/bank/views/actions.fxml");
    }

    @FXML
    private void showClientsView() {
        loadSubView("/com/bank/views/clients.fxml");
    }

    private void loadSubView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading sub-view path: " + fxmlPath);
        }
    }
}
