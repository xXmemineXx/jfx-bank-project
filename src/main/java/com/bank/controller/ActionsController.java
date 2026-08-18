package com.bank.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import com.bank.helpers.ActionCard;
import com.bank.dao.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class ActionsController {

    private String currentViewPath = "/com/bank/views/loan.fxml";

    @FXML
    public void initialize() {
        // Automatically execute the refresh routine every 3 seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            System.out.println("Auto-refreshing view from database...");
            refreshCurrentView(); 
        }));
        
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play(); // Start the background clock loop
    }

    @FXML
    private VBox actionCardsGrid; // Injected fx:id from your ScrollPane content area

    // Instantiate your separate DAOs
    private final LoansDAO loanDAO = new LoansDAO();
    private final TransferDAO transferDAO = new TransferDAO();
    private final ReturnsDAO returnsDAO = new ReturnsDAO();

    // 'loans' button on the left sidebar
    @FXML
    private void viewLoans() {
        List<?> databaseData = loanDAO.getAllLoans();
        currentViewPath = "/com/bank/views/loan.fxml";
        loadDynamicContent("/com/bank/views/loan.fxml", databaseData);
    }

    // 'transfer' button on the left sidebar
    @FXML
    private void viewTransfers() {
        List<?> databaseData = transferDAO.getAllTransfers();
        currentViewPath = "/com/bank/views/transfer.fxml";
        loadDynamicContent("/com/bank/views/transfer.fxml", databaseData);
    }

    // 'return' button on the left sidebar
    @FXML
    private void viewReturns() {
    	List<?> databaseData = returnsDAO.getAllReturns();
        currentViewPath = "/com/bank/views/return.fxml";
        loadDynamicContent("/com/bank/views/return.fxml", databaseData);
    }


    private void loadDynamicContent(String fxmlPath, List<?> dataList) {
        // Clear out the previous layout completely
        actionCardsGrid.getChildren().clear();

        for (Object item : dataList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                
                // Matches whatever your card's root template wrapper is (HBox/VBox)
                Node UIElement = loader.load(); 

                // Get the controller via our safe polymorphism structure
                ActionCard cardController = loader.getController();
                cardController.populateCardData(item);

                // Add configured child object straight into your viewport
                actionCardsGrid.getChildren().add(UIElement);

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Could not load element: " + fxmlPath);
            }
        }
    }

    private void refreshCurrentView() {
    if (currentViewPath.contains("loan")) {
        loadDynamicContent(currentViewPath, loanDAO.getAllLoans());
    } else if (currentViewPath.contains("transfer")) {
        loadDynamicContent(currentViewPath, transferDAO.getAllTransfers());
    }
    }
}
