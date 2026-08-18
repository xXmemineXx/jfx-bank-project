package com.bank.controller;

import com.bank.helpers.ActionCard;
import com.bank.dao.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.HBox; // or HBox depending on card root node
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class ActionsController {

    @FXML
    private VBox actionCardsGrid; // Injected fx:id from your ScrollPane content area

    // Instantiate your separate DAOs
    private final LoansDAO loanDAO = new LoansDAO();
    private final TransferDAO transferDAO = new TransferDAO();

    // 'loans' button on the left sidebar
    @FXML
    private void viewLoans() {
        List<?> databaseData = loanDAO.getAllLoans();
        loadDynamicContent("/com/bank/views/loan.fxml", databaseData);
    }

    // 'transfer' button on the left sidebar
    @FXML
    private void viewTransfers() {
        List<?> databaseData = transferDAO.getAllTransfers();
        loadDynamicContent("/com/bank/views/transfer.fxml", databaseData);
    }

    /**
     * Core Architectural Pipeline method
     */
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
}
